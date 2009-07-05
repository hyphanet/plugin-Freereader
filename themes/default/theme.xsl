<?xml version="1.0"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:datetime="http://exslt.org/dates-and-times"
	version="1.0">

	<xsl:output method="xml" encoding="UTF-8" indent="yes" 
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" 
		doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"/> 

	<xsl:template match="/freereader" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom">
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
				<meta http-equiv="Content-Style-Type" content="text/css" />
				<link rel="stylesheet" type="text/css" media="all" href="assets/style.css" />
				<link rel="shortcut icon" href="assets/feed.png" type="image/png" />

				<title>Freereader</title>
			</head>
			<body>
				<div id="hd">
					<h1>Freereader</h1>
					<p>- contribute your feeds to freenet -</p>
					<p class="updated">
						Last update:
						<xsl:call-template name="printDateTime">
							<xsl:with-param name="dateTime" select="datetime:date-time()"/>
						</xsl:call-template>
					</p>
				</div>
				<div id="sb">
					<h2>Available Feeds</h2>
					<ul>
						<xsl:for-each select="feeds/feed">
							<xsl:sort select="title" />
							<li>
								<xsl:if test="@selected='true'">
									<xsl:attribute name="class">
										active
									</xsl:attribute>
								</xsl:if>
								<a>
									<xsl:attribute name="href">
										<xsl:value-of select="href" />
									</xsl:attribute>
									<xsl:value-of select="title" />
								</a>
							</li>
						</xsl:for-each>
					</ul>
				</div>
				<div id="bd">
					<xsl:for-each  select="current_feed/atom:feed/atom:entry">
						<div class="entry">
							<xsl:choose>
								<xsl:when test="atom:published">
									<div class="entry-date">
									 	<xsl:call-template name="printDateTime">
											<xsl:with-param name="dateTime" select="atom:published"/>
										</xsl:call-template>
									</div>	
								</xsl:when>
								<xsl:otherwise>
									<xsl:choose>
										<xsl:when test="atom:updated">
											<div class="entry-date">
												<xsl:call-template name="printDateTime">
													<xsl:with-param name="dateTime" select="atom:updated"/>
												</xsl:call-template>
											</div>
										</xsl:when>
										<xsl:otherwise>
											<xsl:if test="dc:date">
												<div class="entry-date">
													<xsl:call-template name="printDateTime">
														<xsl:with-param name="dateTime" select="dc:date"/>
													</xsl:call-template>
												</div>
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>	
							<h2><xsl:value-of select="atom:title" /></h2>
							<xsl:choose>
								<xsl:when test="atom:author/atom:name">
									<div class="entry-author">by <xsl:value-of select="atom:author/atom:name" /></div>
								</xsl:when>
								<xsl:otherwise>
									<xsl:if test="dc:creator">
										<div class="entry-author">by <xsl:value-of select="dc:creator" /></div>
									</xsl:if>
								</xsl:otherwise>
							</xsl:choose>
							<div class="entry-body">
								<xsl:choose>
									<xsl:when test="atom:content">
										<xsl:value-of select="atom:content" disable-output-escaping="yes" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:choose>
											<xsl:when test="atom:summary">
												<xsl:value-of select="atom:summary" disable-output-escaping="yes" />
											</xsl:when>
											<xsl:otherwise>
												&#160;
											</xsl:otherwise>
										</xsl:choose>
									</xsl:otherwise>
								</xsl:choose>
							</div>
						</div>
					</xsl:for-each>
					<xsl:choose>
        				<xsl:when test="current_feed/atom:feed/atom:entry" />
        				<xsl:otherwise>
							<div class="message">
								There are no items in this feed.
							</div>
        				</xsl:otherwise>
      				</xsl:choose>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="printDateTime">
		<xsl:param name="dateTime" select="''"/>  
		
		<xsl:variable name="dateMM" select="substring($dateTime, 6, 2)"/>  
		<xsl:variable name="dateDD" select="substring($dateTime, 9, 2)"/>  
		<xsl:variable name="dateYYYY" select="substring($dateTime, 1, 4)"/>   
		<xsl:variable name="date" select="concat($dateMM,'-',$dateDD,'-',$dateYYYY)"/>  
		
		<xsl:variable name="timeHH" select="substring($dateTime, 12, 2)"/>  
		<xsl:variable name="timeMM" select="substring($dateTime, 15, 2)"/>  
		<xsl:variable name="timeSS" select="substring($dateTime, 18, 2)"/>   
		<xsl:variable name="time" select="concat($timeHH,':',$timeMM,':',$timeSS)"/>  
		  
		<xsl:value-of select="$date"/>&#160;<xsl:value-of select="$time"/>
	</xsl:template>

</xsl:stylesheet>

