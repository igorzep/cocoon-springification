<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:form="http://www.springframework.org/tags/form">

<xsl:template match="/">
	<html>
		<head><title>Edit Record Sample</title></head>
		<body>
			<xsl:call-template name="record"/>
		</body>
	</html>
</xsl:template>

<xsl:template name="record">
	<form:form modelAttribute="record">
		<table>
			<tr>
				<td>Id:</td>
				<td><xsl:value-of select="/request/attributes/attribute[@name='record']/record/id"/></td>
				<td/>
			</tr>
			<tr>
				<td>Name:</td>
				<td><form:input path="name"/></td>
				<td><form:errors path="name"/></td>
			</tr>
		</table>
		<input type="submit" value="Save"/>
	</form:form>
</xsl:template>

</xsl:stylesheet>
