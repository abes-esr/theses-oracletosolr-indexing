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

---------------------------------------------------------------------------------
-- cmd to verify which acl are subscribing
---------------------------------------------------------------------------------

SELECT host, lower_port, upper_port, acl
FROM   dba_network_acls
ORDER BY host;