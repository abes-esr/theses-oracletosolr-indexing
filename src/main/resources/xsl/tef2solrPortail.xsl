<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:tef="http://www.abes.fr/abes/documents/tef"
    xmlns:tefextension="http://www.abes.fr/abes/documents/tefextension"
    xmlns:suj="http://www.theses.fr/namespace/sujets"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:date="http://exslt.org/dates-and-times" 
    version="1.0">
    <xsl:output indent="yes"/>
    <xsl:param name="idDeLaBase"/>
    <xsl:param name="texte"></xsl:param>
    <xsl:param name="dateInsertion"></xsl:param>
    <xsl:template match="/">
        <add>
            <doc>
                <!-- plein texte -->

                <field name="textes">
                        <xsl:value-of select="$texte"/>
                </field>

                <!-- id -->

                <field name="id">
                    <xsl:value-of select="$idDeLaBase"/>
                </field>

                <!-- date de premiere insertion dans la base portail-->                
                <field name="dateInsert"><xsl:value-of select="$dateInsertion"/></field>
                                
	  <!-- date de mise a jour-->
        <xsl:if test="//traitements[1]/maj[1]/@date!='' and //traitements[1]/maj[1]/@date!='--T::Z'">
	  	<field name="dateMaj">
	  	    <xsl:value-of select="//traitements[1]/maj[1]/@date"/>
	  	    <xsl:if test="not(contains(//traitements[1]/maj[1]/@date,'Z'))">
	  	        <xsl:text>Z</xsl:text>
	  	    </xsl:if>
	  	</field>
	  </xsl:if>			

                <!-- status = soutenue ou enCours -->
                <xsl:variable name="status">
                    <xsl:choose>
                        <xsl:when test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/dc:identifier[@xsi:type='tef:NNT'] and not(/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/step_gestion)">
                            <xsl:text>soutenue</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>                            
                            <xsl:text>enCours</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <field name="status">
                    <xsl:value-of select="$status"/>
                </field>
                
                <!-- ACT : ajout groupe pour CGE -->
                <xsl:if test="$status='enCours'">                    
                    <xsl:variable name="mappingGroupe" select="document('mapping_groupePortail.xml') "/>
                    <xsl:variable name="ppnEtabSoutenance" select="//tef:thesisAdmin/tef:thesis.degree/tef:thesis.degree.grantor[1]/tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                    <xsl:if test="$mappingGroupe/etablissements/etablissement/ppn[text()=$ppnEtabSoutenance]/../cge/text()='oui'" >
                        <field name="groupe">
                            <xsl:text>cge</xsl:text>
                        </field>
                    </xsl:if>
                </xsl:if>


		  <!-- tous les ppn -->								
		  <xsl:for-each select="//tef:autoriteExterne[@autoriteSource='Sudoc']">  
			<xsl:if test="text()!=''">                  
		      		<field name="ppn">
       	                 <xsl:value-of select="text()"/>
	                    </field>
		  	</xsl:if>
                </xsl:for-each>

		   <xsl:for-each select="//tef:elementdEntree[@autoriteSource='Sudoc']">
				  <xsl:if test="text()!=''">
							<field name="ppn">
					 <xsl:value-of select="@autoriteExterne"/>
						</field>
					</xsl:if>
			</xsl:for-each>

		  <xsl:for-each select="//tef:subdivision[@autoriteSource='Sudoc']">
			  <xsl:if test="text()!=''">
						<field name="ppn">
				 <xsl:value-of select="@autoriteExterne"/>
					</field>
				</xsl:if>
			</xsl:for-each>
		  
		  <field name="source">
		      <xsl:choose>
                        <xsl:when test="$status='soutenue' and /mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/star_gestion/traitements/sorties/cines/@indicCines='OK'">
                            <xsl:text>star</xsl:text>
                        </xsl:when>
			   <xsl:when test="$status='enCours'">
                            <xsl:text>step</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>                            
                            <xsl:text>sudoc</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </field>

                

                <!-- accessible en ligne ? -->

                <xsl:variable name="cas">
                    <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/star_gestion/traitements/@scenario"/>
                </xsl:variable>

                <xsl:variable name="now" select="translate(date:date(), '-', '')"/>
                
                <xsl:variable name="finRestriction">
                    <xsl:value-of select="translate(/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/star_gestion/traitements/sorties/diffusion/@restrictionTemporelleFin, '-', '')"/>
                </xsl:variable>
                            
                <field name="accessible">
                    <xsl:choose>
                        <xsl:when test="$cas='cas1' or $cas='cas2' or $cas='cas3' or $cas='cas4' ">
                            <xsl:choose>
                                <xsl:when test="($finRestriction='' or $finRestriction &lt;= $now)">
                                    <xsl:text>oui</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>non</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>                                                       
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>non</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </field>

                <!-- titre en français -->

                <field name="titre">
                    <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dc:title"/>
                </field>

                <!-- titre en langue étrangère -->

                <field name="titre2">
                    <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dcterms:alternative"/>
                </field>

                <!-- auteur -->                
                <field name="auteurPpn">
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                </field>
                <field name="auteur">
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:prenom"/>
                    
                    <xsl:text> </xsl:text>
                    
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nom"/>

                    <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance/text()!='' and (/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance != /mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nom)">
                   
                        <xsl:text> (</xsl:text>
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance"/>
                        <xsl:text>)</xsl:text>
                    </xsl:if>                    
                </field> 
                <!-- auteur Nom Prenom-->               
                <field name="auteurNP">                    
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nom"/>

                    <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance/text()!='' and (/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance != /mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nom)">
                   
                        <xsl:text> (</xsl:text>
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance"/>
                        <xsl:text>)</xsl:text>
                    </xsl:if>   			
			<xsl:text> </xsl:text>			
			<xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:prenom"/>                                                                             
                </field>                 
                <field name="personne">                                        
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nom"/>
                    
                    <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance/text()!='' and (/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance != /mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nom)">
                        
                        <xsl:text> (</xsl:text>
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:nomDeNaissance"/>
                        <xsl:text>)</xsl:text>
                    </xsl:if>                    
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:auteur/tef:prenom"/>                                        
                </field> 
                              
                <!-- co auteurs-->
                <xsl:for-each select="//tefextension:coAuteur">
                        <field name="coAuteurPpn">
                            <xsl:value-of select="tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                        </field>
                        <field name="coAuteur">
                            <xsl:value-of select="tef:prenom"/>
                            
                            <xsl:text> </xsl:text>
                            
                            <xsl:value-of select="tef:nom"/>
                            
                            <xsl:if test="tef:nomDeNaissance/text()!='' and (tef:nomDeNaissance != tef:nom)">
                                
                                <xsl:text> (</xsl:text>
                                <xsl:value-of select="tef:nomDeNaissance"/>
                                <xsl:text>)</xsl:text>
                            </xsl:if>                    
                        </field> 
                                  
                        <field name="coAuteurNP">                    
                            <xsl:value-of select="tef:nom"/>                            
                            <xsl:if test="tef:nomDeNaissance/text()!='' and (tef:nomDeNaissance != tef:nom)">                                
                                <xsl:text> (</xsl:text>
                                <xsl:value-of select="tef:nomDeNaissance"/>
                                <xsl:text>)</xsl:text>
                            </xsl:if>   			
                            <xsl:text> </xsl:text>			
                            <xsl:value-of select="tef:prenom"/>                                                                             
                        </field>    
                    
                        <field name="personne">                                        
                            <xsl:value-of select="tef:nom"/>
                            
                            <xsl:if test="tef:nomDeNaissance/text()!='' and (tef:nomDeNaissance != tef:nom)">
                                
                                <xsl:text> (</xsl:text>
                                <xsl:value-of select="tef:nomDeNaissance"/>
                                <xsl:text>)</xsl:text>
                            </xsl:if>                    
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="tef:prenom"/>                                        
                        </field> 
                </xsl:for-each>

                <!-- directeur thèse -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:directeurThese">                  
                   <field name="directeurThese">                       
                        <xsl:value-of select="tef:prenom"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="tef:nom"/>                        
                   </field>                    
	          <field name="directeurThesePpn">
                        <xsl:value-of select="tef:autoriteExterne[@autoriteSource='Sudoc']"/>
	          </field>
                    <field name="personne">                       
                        <xsl:value-of select="tef:nom"/>         
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="tef:prenom"/>                                                
                    </field>                    
                </xsl:for-each>
                
                <!-- directeur thèse Nom Prenom-->
                
                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:directeurThese">                  
                    <field name="directeurTheseNP">                                               
                        <xsl:value-of select="tef:nom"/>     
                        <xsl:text> </xsl:text>                        
                        <xsl:value-of select="tef:prenom"/>                                               
                    </field>
                </xsl:for-each>

		  <!-- president du jury -->
                
                <field name="presidentJury">                    
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:presidentJury/tef:prenom"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:presidentJury/tef:nom"/>                    
                </field>
                
                <!-- president du jury Ppn -->
                
                <field name="presidentJuryPpn">
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:presidentJury/tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                </field>
                
                <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:presidentJury/tef:nom/text()!=''">
                    <field name="personne">                    
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:presidentJury/tef:nom"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:presidentJury/tef:prenom"/>                                        
                    </field>
                </xsl:if>                              

                <!-- rapporteurs -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:rapporteur">
                    <field name="rapporteur">                        
                        <xsl:value-of select="tef:prenom"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="tef:nom"/>                        
                    </field>                    
                    <field name="rapporteurPpn">
                        <xsl:value-of select="tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                    </field>      
                    <field name="personne">
                        <xsl:value-of select="tef:nom"/>                           
                        <xsl:text> </xsl:text>                                                
                        <xsl:value-of select="tef:prenom"/>                     
                    </field>                          
                </xsl:for-each>
				
				<!-- rapporteurs Nom Prenom-->
                
                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:rapporteur">                  
                    <field name="rapporteurNP">                                               
                        <xsl:value-of select="tef:nom"/>     
                        <xsl:text> </xsl:text>                        
                        <xsl:value-of select="tef:prenom"/>                                               
                    </field>
                </xsl:for-each>
	  <!-- membres du jury -->
                
                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:membreJury">
                    <field name="membreJury">                        
                        <xsl:value-of select="tef:prenom"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="tef:nom"/>                        
                    </field>
                    <field name="membreJuryPpn">
                        <xsl:value-of select="tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                    </field>                
                </xsl:for-each>

                <!-- établissement de soutenance -->

                <field name="etabSoutenance">
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:thesis.degree/tef:thesis.degree.grantor[1]/tef:nom"/>
                </field>                
                <field name="etabSoutenancePpn">
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:thesis.degree/tef:thesis.degree.grantor[1]/tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                </field>

                <!-- code etab -->

                <field name="codeEtab">
                    <xsl:choose>
                        <xsl:when test="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/star_gestion/@codeEtab">
                            <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/star_gestion/@codeEtab"/>
                        </xsl:when>
                        <xsl:when test="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/step_gestion/@codeEtab">
                            <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/step_gestion/@codeEtab"/>
                        </xsl:when>
                    </xsl:choose>                                      
                </field>
                
                <!-- co-tutelle -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:thesis.degree/tef:thesis.degree.grantor[2]">
                    <xsl:if test="tef:nom">
                        <field name="coTutelle">
                            <xsl:value-of select="tef:nom"/>
                        </field>
                        <field name="coTutellePpn">
                            <xsl:value-of select="tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                        </field>
                    </xsl:if>                    
                </xsl:for-each>

                <!-- ecoles doctorales -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:ecoleDoctorale">
                    <field name="ecoleDoctorale">
                        <xsl:value-of select="tef:nom"/>
                    </field>
                    <field name="ecoleDoctoralePpn">
                        <xsl:value-of select="tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                    </field>                    
                </xsl:for-each>

                <!-- partenaires de recherche -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche">
                    <field name="partRecherche">
                        <xsl:value-of select="tef:nom"/>
                    </field>
                    <field name="partRecherchePpn">
                        <xsl:value-of select="tef:autoriteExterne[@autoriteSource='Sudoc']"/>
                    </field>		                        
                </xsl:for-each>

                <!-- types partenaire de recherche -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche/@type">
                    <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche/@type">
                        <field name="typePartRecherche">
                            <xsl:value-of select="text()"/>
                        </field>
                    </xsl:if>
                    
                </xsl:for-each>

                <!-- partenaire -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche">
                    <field name="partenaire">
                        <xsl:value-of select="tef:nom"/>
                        <xsl:text>, </xsl:text>
                        <xsl:value-of select="@type"/>
                    </field>
                </xsl:for-each>
                
                <!-- partenaire labo -->
                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche[@type='laboratoire']">
                    <field name="partenaireLabo">
                        <xsl:value-of select="tef:nom"/>                    
                    </field>
                </xsl:for-each>
                
                <!-- partenaire entreprise -->
                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche[@type='entreprise']">
                    <field name="partenaireEntreprise">
                        <xsl:value-of select="tef:nom"/>                    
                    </field>
                </xsl:for-each>
                
                <!-- partenaire fondation -->
                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche[@type='fondation']">
                    <field name="partenaireFondation">
                        <xsl:value-of select="tef:nom"/>                    
                    </field>
                </xsl:for-each>
                
                <!-- partenaire equipeRecherche -->
                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche[@type='equipeRecherche']">
                    <field name="partenaireEquipeDeRecherche">
                        <xsl:value-of select="tef:nom"/>                    
                    </field>
                </xsl:for-each>
                
                <!-- partenaire autreType -->
                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:partenaireRecherche[@type='autreType']">
                    <field name="partenaireAutre">
                        <xsl:value-of select="tef:nom"/>                    
                    </field>
                </xsl:for-each>

                <!-- editeurs -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:edition/tef:editeur/tef:nom">
                    <field name="editeur">
                        <xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>

                
                <xsl:variable name="dateSoutenanceValide">
                    <xsl:call-template name="date-is-valid">
                        <!-- Returns "1" -->
                        <xsl:with-param name="year" select="substring(//tef:thesisAdmin/dcterms:dateAccepted,1,4)"/>
                        <xsl:with-param name="month" select="substring(//tef:thesisAdmin/dcterms:dateAccepted,6,2)"/>
                        <xsl:with-param name="day" select="substring(//tef:thesisAdmin/dcterms:dateAccepted,9,2)"/>
                    </xsl:call-template>
                </xsl:variable>
                
                <!-- date de soutenance -->
                <xsl:if test="$dateSoutenanceValide=1">
                    <field name="dateSoutenance">
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/dcterms:dateAccepted"/>
                        <xsl:text>T23:59:59Z</xsl:text>
                    </field>
                </xsl:if>
                
                <!-- Cumul : date de soutenance + date de la soutenance prevue (SUJET)-->
                <xsl:if test="$dateSoutenanceValide=1  or /mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:datePrevue/text()!=''">
                    <field name="dateSoutenanceCumul">
                        <xsl:choose>
                            <xsl:when test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/dcterms:dateAccepted/text()!=''">
                                <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/dcterms:dateAccepted"/>
                                <xsl:text>T23:59:59Z</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:datePrevue"/>                        
                                <xsl:text>T</xsl:text>
                                <xsl:choose>
                                    <xsl:when test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:heurePrevue/text()!=''">
                                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:heurePrevue"/>
                                    </xsl:when>
                                    <xsl:otherwise><xsl:text>23:59</xsl:text></xsl:otherwise>
                                </xsl:choose>
                                <xsl:text>:59Z</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>                                                
                    </field>
                </xsl:if>
                
                <!-- date de premiere inscription du doctorat (SUJET) -->
                <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:thesis.degree/suj:datePremiereInscriptionDoctorat/text()!=''">
                    <field name="sujDatePremiereInscription">
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:thesis.degree/suj:datePremiereInscriptionDoctorat"/>
                        <xsl:text>T23:59:59Z</xsl:text>
                    </field>
                </xsl:if>
                
                <!-- date de la soutenance prevue (SUJET)-->                
                <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:datePrevue/text()!=''">
                    <field name="sujDateSoutenancePrevue">
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:datePrevue"/>                        
                        <xsl:text>T</xsl:text>
                        <xsl:choose>
                            <xsl:when test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:heurePrevue/text()!=''">
                                <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:heurePrevue"/>
                            </xsl:when>
                            <xsl:otherwise><xsl:text>23:59</xsl:text></xsl:otherwise>
                        </xsl:choose>
                        <xsl:text>:59Z</xsl:text>
                    </field>
                </xsl:if>
                
                <!-- lieu de la soutenance prevue (SUJET)-->                
                <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:lieuPrevu/text()!=''">
                    <field name="sujSoutenanceLieuPrevu">
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:lieuPrevu"/>                                                
                    </field>
                </xsl:if>
                
                <!-- publicite de la soutenance prevue (SUJET)-->                
                <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:publiciteSoutenance/text()!=''">
                    <field name="sujSoutenancePublicite">
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:soutenancePrevue/suj:publiciteSoutenance"/>                                                
                    </field>
                </xsl:if>
                
                <!-- date de l'abandon (SUJET) -->
                <xsl:if test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:dateAbandon/text()!=''">
                    <field name="sujDateAbandon">
                        <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/suj:vie/suj:dateAbandon"/>
                        <xsl:text>T23:59:59Z</xsl:text>
                    </field>
                </xsl:if>                
                
                <!-- oaisets -->

                <xsl:for-each select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:oaiSetSpec">
                    <field name="oaiSetSpec">
                        <xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>

                <!-- discipline -->

                <field name="discipline">
                    <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/tef:thesis.degree/tef:thesis.degree.discipline"/>
                </field>

                <!-- subjects en français -->

                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dc:subject[@xml:lang='fr']">
                    <field name="subjectFR">
                        <xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>

                <!-- subjects en anglais -->

                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dc:subject[@xml:lang='en']">
                    <field name="subjectEN">
                        <xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>
                
                <!-- subjects en espagnol -->
                
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dc:subject[@xml:lang='es']">
                    <field name="subjectES">
                        <xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>
                
                <!-- autres subjects -->
                
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dc:subject[@xml:lang!='fr' and @xml:lang!='en' and @xml:lang!='es']">
                    <field name="subjectXX">
                        <xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>

                <!-- abstract en français -->

                <field name="abstractFR">
                    <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dcterms:abstract[@xml:lang='fr']"/>
                </field>

                <!-- abstract en anglais -->

                <field name="abstractEN">
                    <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dcterms:abstract[@xml:lang='en']"/>
                </field>
                
                <!-- abstract en espagnol -->
                
                <xsl:if test="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dcterms:abstract[@xml:lang='es']">
                    <field name="abstractES">
                        <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dcterms:abstract[@xml:lang='es']"/>
                    </field>
                </xsl:if>
                
                <!-- autres abstract -->
                
                <xsl:if test="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dcterms:abstract[@xml:lang!='fr' and @xml:lang!='en' and @xml:lang!='es']">
                    <field name="abstractXX">
                        <xsl:value-of select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dcterms:abstract[@xml:lang!='fr' and @xml:lang!='en' and @xml:lang!='es']"/>
                    </field>
                </xsl:if>

                <!-- langues de la thèse -->

                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dc:language">
                    <field name="langueThese">
                        <xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>

                <!-- type de doc -->

                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/dc:type">
                    <field name="typeDeDoc">
                        <xsl:value-of select="text()"/>
                    </field>
                </xsl:for-each>

	  <!-- num --> 

                <field name="num">
                    <xsl:choose>
                        <xsl:when test="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/dc:identifier[@xsi:type='tef:NNT'] and not(/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/step_gestion)">
                            <xsl:value-of select="/mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/tef:thesisAdmin/dc:identifier[@xsi:type='tef:NNT']"/>
                        </xsl:when>
                        <xsl:when test="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/step_gestion/@ID_SUJET">
                            <xsl:text>s</xsl:text><xsl:value-of select="substring-after(/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/step_gestion/@ID_SUJET,'SUJET_')"/>
                        </xsl:when>                        
                    </xsl:choose>                    
                </field>


                <!-- these sur trvx -->
                        
                <field name="theseSurTravaux">
                            <xsl:value-of select="/mets:mets/mets:amdSec[1]/mets:techMD[1]/mets:mdWrap[1]/mets:xmlData[1]/tef:thesisAdmin[1]/tef:theseSurTravaux[1]"/>
                </field>
                
                <!--  pour les sujetRameau... -->
                
                <!--  vedetteRameauPersonne -->
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauPersonne/tef:elementdEntree">
                    <field name="vedettePersonneElemEntree">
                        <xsl:value-of select="."/>
                    </field>
                </xsl:for-each>          
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauPersonne/tef:subdivision[@type!='subdivisionDeForme']">
                    <field name="vedettePersonneSubDiv">
                        <xsl:value-of select="."/>
                    </field>  
                </xsl:for-each>
                
                <!--  vedetteRameauNomCommun -->
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauNomCommun/tef:elementdEntree">
                    <field name="vedetteNomCommunElemEntree">
                        <xsl:value-of select="."/>
                    </field>
                </xsl:for-each>                    
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauNomCommun/tef:subdivision[@type!='subdivisionDeForme']">
                    <field name="vedetteNomCommunSubDiv">
                        <xsl:value-of select="."/>
                    </field>  
                </xsl:for-each>      
                     
                <!--  vedetteRameauCollectivite -->
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauCollectivite/tef:elementdEntree">
                    <field name="vedetteCollectiviteElemEntree">
                        <xsl:value-of select="."/>
                    </field>
                </xsl:for-each>                    
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauCollectivite/tef:subdivision[@type!='subdivisionDeForme']">
                    <field name="vedetteCollectiviteSubDiv">
                        <xsl:value-of select="."/>
                    </field>  
                </xsl:for-each>                   

                <!--  vedetteRameauFamille -->
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauFamille/tef:elementdEntree">
                    <field name="vedetteFamilleElemEntree">
                        <xsl:value-of select="."/>
                    </field>
                </xsl:for-each>                    
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauFamille/tef:subdivision[@type!='subdivisionDeForme']">
                    <field name="vedetteFamilleSubDiv">
                        <xsl:value-of select="."/>
                    </field>  
                </xsl:for-each>                              
                    
                <!--  vedetteRameauAuteurTitre -->
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauAuteurTitre/tef:elementdEntree">
                    <field name="vedetteAuteurTitreElemEntree">
                        <xsl:value-of select="."/>
                    </field>
                </xsl:for-each>                    
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauAuteurTitre/tef:subdivision[@type!='subdivisionDeForme']">
                    <field name="vedetteAuteurTitreSubDiv">
                        <xsl:value-of select="."/>
                    </field>  
                </xsl:for-each>      
                    
                <!--  vedetteRameauTitre -->
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauTitre/tef:elementdEntree">
                    <field name="vedetteTitreElemEntree">
                        <xsl:value-of select="."/>
                    </field>
                </xsl:for-each>                    
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauTitre/tef:subdivision[@type!='subdivisionDeForme']">
                    <field name="vedetteTitreSubDiv">
                        <xsl:value-of select="."/>
                    </field>  
                </xsl:for-each>          
                    
                <!--  vedetteRameauNomGeographique -->
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauNomGeographique/tef:elementdEntree">
                    <field name="vedetteGeographiqueElemEntree">
                        <xsl:value-of select="."/>
                    </field>
                </xsl:for-each>                    
                <xsl:for-each select="/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/tef:thesisRecord/tef:sujetRameau/tef:vedetteRameauNomGeographique/tef:subdivision[@type!='subdivisionDeForme']">
                    <field name="vedetteGeographiqueSubDiv">
                        <xsl:value-of select="."/>
                    </field>  
                </xsl:for-each>                           

                <!-- Données de gestion -->

                <!-- <xsl:call-template name="traiterStarGestion"/> -->

            </doc>
        </add>
    </xsl:template>
    
    <xsl:template name="date-is-valid">
        <xsl:param name="year" select="''"/>
        <xsl:param name="month" select="''"/>
        <xsl:param name="day" select="''"/>
        
        <xsl:variable name="year-is-integer" select="number($year) = $year and floor($year) = $year" />
        <xsl:variable name="month-is-integer" select="number($month) = $month and floor($month) = $month" />
        <xsl:variable name="day-is-integer" select="number($day) = $day and floor($day) = $day" />
        
        <xsl:choose>
            <xsl:when test="
                not($year-is-integer) or not($month-is-integer) or not($day-is-integer) or
                $year &lt; 1970 or $month &lt; 1 or $month &gt; 12 or
                $day &lt; 1 or $day &gt; 31 or (
                ($year mod 4 = 0 and $month = 2 and $day &gt; 29) or
                ($year mod 4 != 0 and $month = 2 and $day &gt; 28) or
                (($month = 4 or $month = 6 or $month = 9 or $month = 11) and $day &gt; 30)
                )">
                <xsl:value-of select="0"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="1"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
