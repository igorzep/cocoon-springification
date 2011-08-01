<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:variable name="attributes" select="/request/attributes/attribute"/>

<xsl:template match="/">
	<html>
		<head><title>Record Sample</title></head>
		<body>
			<xsl:apply-templates select="$attributes[@name = 'record']/record"/>
		</body>
		<br/>
	</html>
</xsl:template>

<xsl:template match="record">
	<table>
		<th colspan="2">Record info:</th>
		<tr>
			<td>Id:</td>
			<td><xsl:value-of select="id"/></td>
		</tr>
		<tr>
			<td>Name:</td>
			<td><xsl:value-of select="name"/></td>
		</tr>
	</table>
	<a href="/record/{id}/edit">Edit</a>
</xsl:template>

</xsl:stylesheet>
