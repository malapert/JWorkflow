/* 
 * Copyright (C) 2014 Jean-Christophe Malapert
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.malapert.jworkflow.validation;

import fr.cnes.sitools.astro.resolver.cds.Resolver;
import fr.cnes.sitools.astro.resolver.cds.Sesame;
import fr.cnes.sitools.astro.resolver.cds.Target;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;

/**
 *
 * @author Jean-Christophe Malapert <jcmalapert@gmail.com>
 */
public class PositionValidation extends Validation {

    /**
     * Template URL for the CDS name resolver service.
     */
    private static final String TEMPLATE_NAME_RESOLVER = "http://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/-oxp/<service>?<objectName>";
    /**
     * Time out of 10 seconds when calling each name resolver.
     */
    protected static final int SERVER_TIMEOUT = 10000;

    /**
     *
     */
    public static final double DEG2ARSEC = 3600;

    /**
     *
     */
    public static final double DEFAULT_PRECISION_DEG = 0.2 / 3600;
    
    private static final String OBJNAME = "OBJNAME";
    private static final String RA = "RA";
    private static final String DEC = "DEC";
    

    private double precisionDeg;

    /**
     *
     */
    public PositionValidation() {
        this(DEFAULT_PRECISION_DEG);
    }

    /**
     *
     * @param precisionDeg
     */
    public PositionValidation(double precisionDeg) {
        org.restlet.engine.Engine.getAnonymousLogger().setLevel(Level.OFF);
        this.precisionDeg = precisionDeg;
    }

    /**
     *
     * @param objectName
     * @return
     * @throws Exception
     */
    public static String[] computePosFromObjectName(String objectName) throws Exception {
        objectName = URLEncoder.encode(objectName, "UTF-8");
        final String urlTmp = TEMPLATE_NAME_RESOLVER.replace("<objectName>", objectName);
        String url = urlTmp.replace("<service>", "A");
        final Sesame sesameResponse = parseResponse(url);
        final String[] coordinates = parseCoordinates(sesameResponse);
        return coordinates;
    }

    private static Sesame parseResponse(final String url) throws Exception  {
        final ClientResource client = new ClientResource(url);        
        final Client clientHTTP = new Client(Protocol.HTTP);
        //clientHTTP.setConnectTimeout(SERVER_TIMEOUT);
        client.setNext(clientHTTP);
        final Status status = client.getStatus();
        if (status.isSuccess()) {
            try {
                final JAXBContext ctx = JAXBContext.newInstance(new Class[]{fr.cnes.sitools.astro.resolver.cds.CDSFactory.class});
                final Unmarshaller unMarshaller = ctx.createUnmarshaller();
                String result = client.get().getText();                
                return (Sesame) unMarshaller.unmarshal(new ByteArrayInputStream(result.getBytes()));
            } catch (JAXBException ex) {
                throw new Exception(ex);
            } finally {
                clientHTTP.stop();
                client.release();
            }
        } else {
            clientHTTP.stop();
            client.release();
            throw new Exception("Cannot access to CDS");
        }
    }

    private static String[] parseCoordinates(final Sesame sesameResponse) throws Exception {
        final Target target = sesameResponse.getTarget().get(0);
        final List<Resolver> resolvers = target.getResolver();
        String[] coordinates = new String[2];
        for (Resolver resolver : resolvers) {
            final List<JAXBElement<?>> terms = resolver.getINFOOrERROROrOid();
            for (JAXBElement<?> term : terms) {
                if (coordinates[0] != null && coordinates[1] != null) {
                    break;
                } else if ("jradeg".equals(term.getName().getLocalPart())) {
                    coordinates[0] = term.getValue().toString();
                } else if ("jdedeg".equals(term.getName().getLocalPart())) {
                    coordinates[1] = term.getValue().toString();
                }
            }
        }
        if (coordinates[0] == null || coordinates[1] == null) {
            throw new Exception("Unknown object");
        }
        return coordinates;
    }
    
    private double[] computeRadec2xyz(double ra, double dec) {
        double x = Math.cos(Math.toRadians(dec)) * Math.cos(Math.toRadians(ra));
        double y = Math.cos(Math.toRadians(dec) * Math.sin(Math.toRadians(ra)));
        double z = Math.sin(Math.toRadians(dec));
        double dist = Math.sqrt(x*x + y*y + z*z);
        return new double[]{x/dist, y/dist, z/dist};
    }
    
    private double computeAngularDist(double ra1, double dec1, double ra2, double dec2) {
        double[] xyz1 = computeRadec2xyz(ra1, dec1);
        double[] xyz2 = computeRadec2xyz(ra2, dec2);
        double dist = Math.acos(xyz1[0] * xyz2[0] + xyz1[1] * xyz2[1] + xyz1[2] * xyz2[2]);
        return Math.toDegrees(dist);
    }

    @Override
    protected boolean processValidation(File file, List<String> error, List<String> warning) {
        boolean result;
        try {
            Fits fits = new Fits(file);
            BasicHDU basicHDU = fits.readHDU();
            Header header = basicHDU.getHeader();
            if (header.containsKey(OBJNAME) && header.containsKey(RA) && header.containsKey(DEC)) {
                String objectName = header.getStringValue(OBJNAME).trim();
                double ra = header.getDoubleValue(RA);
                double dec = header.getDoubleValue(DEC);                
                String[] coord = computePosFromObjectName(objectName);
                double dist = computeAngularDist(ra, dec, Double.valueOf(coord[0]), Double.valueOf(coord[1]));
                if (dist < precisionDeg) {
                    Logger.getLogger(PositionValidation.class.getName()).log(Level.INFO, "{0} is "
                            + "valid", file.getName());
                    result = true;
                } else {
                    Logger.getLogger(PositionValidation.class.getName()).log(Level.INFO, "{0} is "
                            + "not valid, the coordinates are too far ({1} degree) from the "
                            + "official coordinates of {2}", new Object[]{file.getName(), dist, objectName});
                    result = false;                    
                }                
            } else {
                result = true;
            }
        } catch (FitsException ex) {
            Logger.getLogger(PositionValidation.class.getName()).log(Level.SEVERE, null, ex);
            throw new ValidationError(ex);
        } catch (IOException ex) {
            Logger.getLogger(PositionValidation.class.getName()).log(Level.SEVERE, null, ex);
            throw new ValidationError(ex);
        } catch (Exception ex) {
            Logger.getLogger(PositionValidation.class.getName()).log(Level.SEVERE, null, ex);
            throw new ValidationError(ex);
        }
        return result;
    }

}
