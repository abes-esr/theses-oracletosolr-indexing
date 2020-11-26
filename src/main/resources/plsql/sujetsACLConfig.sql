-- acl configuration

BEGIN
dbms_network_acl_admin.create_acl(acl => 'pkg.xml',
                                description => 'Normal Access',
                                principal => 'XXXXXX', --user name
                                is_grant => TRUE,
                                privilege => 'connect',
                                start_date => NULL,
                                end_date => NULL);
END;

------------------------------------------------------------------------------


BEGIN
dbms_network_acl_admin.add_privilege(acl => 'pkg.xml',
							   principal => 'XXXXXX', -- user name
							   is_grant => TRUE,
							   privilege => 'connect',
							   start_date => NULL,
							   end_date => NULL);
END;


--------------------------------------------------------------------------------



BEGIN
      dbms_network_acl_admin.assign_acl(acl => 'pkg.xml',
                                host => 'xxxxxxxxxx.fr',--  Web Server URL
                                lower_port => 8100, -- Web  Server PORTs
                                upper_port => 8100);
END;


-----------------------------------------------------------------
--https://stackoverrun.com/fr/q/8851928
--http://c2anton.blogspot.com/2016/03/apexwebservicemakerestrequest-not.html
--https://stackoverflow.com/questions/58802342/how-to-invoke-a-rest-web-service-with-post-method-sending-the-parameters-as-jso
BEGIN
       DBMS_NETWORK_ACL_ADMIN.APPEND_HOST_ACE(
           host => '*',
           ace => xs$ace_type(privilege_list => xs$name_list('connect'),
                              principal_name => 'apex_040200',
                              principal_type => xs_acl.ptype_db));
END;


---------------------------------------------------------------------------------
-- cmd to verify which acl are subscribing
---------------------------------------------------------------------------------

SELECT host, lower_port, upper_port, acl
FROM   dba_network_acls
ORDER BY host;