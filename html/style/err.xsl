<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" encoding="UTF-8"/>

<xsl:template match="/">
 <xsl:apply-templates/>
</xsl:template> 

<xsl:template match="div">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="/style/main.css" />
</head>
<body>
<xsl:apply-templates/>
<hr/>
</body>
</html>
</xsl:template> 

<xsl:template match="p">
ERROR: <xsl:value-of select="." />
<BR/>
</xsl:template> 

<xsl:template match="b">
WARNING: <xsl:value-of select="." />
<BR/>
</xsl:template>

</xsl:stylesheet>
