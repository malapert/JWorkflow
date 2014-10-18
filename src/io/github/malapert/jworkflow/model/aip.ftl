<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2014 Jean-Christophe Malapert

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->

<AIP xmlns:SIMone="http://simone.com/simone/v1" xmlns="http://simone.com/aip/v1">
    <core>
<#list core?keys as key><#assign value = core[key]>
        <${key}>${value}</${key}>
</#list>
    </core>
    <metadata>
<#list metadata?keys as keyTopic><#assign valueTopic = metadata[keyTopic]>
        <${keyTopic}>
    <#list valueTopic?keys as key><#assign value = valueTopic[key]>
            <${key}>${value}</${key}>
    </#list>
        </${keyTopic}>
</#list>
    </metadata>
    <recordMgt>
<#list recordsMgt as recordMgt>
        <record>![CDATA[${recordMgt}]</record>
</#list>
    </recordMgt>
</AIP>