--------------------------------------------------------------------------------
-- if the project is in java 1.8 with tomcat8
--------------------------------------------------------------------------------


create or replace PROCEDURE indexationsolr(iddoc in number, docparam in xmltype)
AS

l_http_request     UTL_HTTP.req;
l_http_response    UTL_HTTP.resp;

l_buffer_size      NUMBER (10)      := 512;
l_line_size        NUMBER (10)      := 50;
l_lines_count      NUMBER (10)      := 20;
l_line             VARCHAR2 (32767);
l_substring_msg    VARCHAR2 (32767);
l_raw_data         RAW (512);
l_clob_response    CLOB;
l_host_name        VARCHAR2 (128)   := 'xxxxxxxxxxxxxx.fr';
l_port             VARCHAR2 (128)   := '8100';
req_length binary_integer;
--bufferY   CLOB;
bufferY varchar2(32767);
--amount  pls_integer:=4000;
amount number :=32767;
offset  pls_integer:=1;
l_contexte_request VARCHAR2 (128)   := 'star';
l_doc_request      CLOB;
l_content  CLOB;
url varchar2(2000);
doc_length binary_integer;
xml_length binary_integer;




BEGIN

  DBMS_OUTPUT.PUT_LINE ( 'debut');
  --select dbms_lob.getlength(xmltype.getclobval(doc))into lengthdoc from DOCUMENT;

  select XMLSERIALIZE (CONTENT docparam as CLOB) into l_doc_request from dual;
  xml_length := DBMS_LOB.getlength (xmltype.getclobval(docparam));
  doc_length := DBMS_LOB.getlength (l_doc_request);

  --print_clob_to_output (l_doc_request);
  l_content := '{"$contexte$":"'||l_contexte_request||'","$iddoc$":"'||iddoc||'","$doc$":"'||l_doc_request||'"}';
  DBMS_OUTPUT.PUT_LINE ( 'iddoc = '|| iddoc);

  req_length := DBMS_LOB.getlength (l_content);

  url := 'http://'
                                                  || l_host_name
                                                  || ':'
                                                  || l_port
                                                  || '/indexationsolr/GetIndexationSolr';
  l_http_request :=
     UTL_HTTP.begin_request (url               =>    url,
                             method            => 'POST',
                             http_version      => 'HTTP/1.1'
                            );

      UTL_HTTP.set_header (l_http_request, 'User-Agent', 'Mozilla/4.0');
      UTL_HTTP.set_header (l_http_request,'Host',l_host_name || ':' || l_port);
      UTL_HTTP.set_header (l_http_request, 'Connection', 'close');
      --UTL_HTTP.set_header(l_http_request, 'content-type', 'application/json;charset=UTF-8');
      utl_http.set_header(l_http_request, 'Content-Type', 'text/xml;charset=UTF-8');
      UTL_HTTP.set_header (l_http_request,'Content-Length',req_length);
      UTL_HTTP.set_header (l_http_request, 'Transfer-Encoding', 'chunked' );
      UTL_HTTP.set_body_charset('utf8');


        offset :=1;
        --bufferY :=null;
        --amount:=4000;

  WHILE (offset < req_length)
   LOOP
      DBMS_LOB.read (l_content,
                     amount,
                     offset,
                     bufferY);
      UTL_HTTP.write_text (l_http_request, bufferY);
      offset := offset + amount;

   END LOOP;
      l_http_response := UTL_HTTP.get_response (l_http_request);
      l_clob_response :=null;
      BEGIN

         <<response_loop>>
         LOOP
            UTL_HTTP.read_raw (l_http_response, l_raw_data, l_buffer_size);
            l_clob_response :=
                     l_clob_response || UTL_RAW.cast_to_varchar2 (l_raw_data);
                     DBMS_OUTPUT.PUT_LINE ( 'l_clob_response = '|| l_clob_response );
         END LOOP response_loop;
      EXCEPTION
         WHEN UTL_HTTP.end_of_body
         THEN
            UTL_HTTP.end_response (l_http_response);
      END;

  IF l_http_request.private_hndl IS NOT NULL
  THEN
     UTL_HTTP.end_request (l_http_request);
  END IF;

  IF l_http_response.private_hndl IS NOT NULL
  THEN
     UTL_HTTP.end_response (l_http_response);
  END IF;

  EXCEPTION
  WHEN UTL_HTTP.request_failed THEN
    UTL_HTTP.end_response(l_http_response);
  WHEN others THEN
    UTL_HTTP.end_response (l_http_response);


END;

--------------------------------------------------------------------------------
-- if the project is in java 11 with tomcat9
--------------------------------------------------------------------------------

create or replace PROCEDURE indexationsolr(iddoc in number)
AS



l_response clob;



BEGIN

    apex_web_service.g_request_headers(1).name := 'Content-Type';
    apex_web_service.g_request_headers(1).value := 'application/json';

    l_response := apex_web_service.make_rest_request(
        p_url => 'http://serveur:port/indexationsolr/GetIndexationSolr?iddoc='||iddoc||'&contexte=star',
        p_http_method => 'GET');


    DBMS_OUTPUT.PUT_LINE(l_response);



END;

--------------------------------------------------------------------------------
-- orpins-p
--------------------------------------------------------------------------------


create or replace PROCEDURE indexationsolr(iddoc in number, docparam in xmltype)
AS

l_http_request     UTL_HTTP.req;
l_http_response    UTL_HTTP.resp;

l_buffer_size      NUMBER (10)      := 512;
l_line_size        NUMBER (10)      := 50;
l_lines_count      NUMBER (10)      := 20;
l_line             VARCHAR2 (32767);
l_substring_msg    VARCHAR2 (32767);
l_raw_data         RAW (512);
l_clob_response    CLOB;
l_host_name        VARCHAR2 (128)   := 'xxxxxxxxxxxxx.fr'; --Web Server URL
l_port             VARCHAR2 (128)   := 'xx'; ----Web Server port
req_length binary_integer;
bufferY   CLOB;
amount  pls_integer:=4000;
offset  pls_integer:=1;
l_contexte_request VARCHAR2 (128)   := 'star';
l_doc_request      CLOB;
l_content  CLOB;
url varchar2(2000);




BEGIN

  --SP_LOG('Beginning indexationsolr proc','');



  select XMLSERIALIZE (CONTENT docparam as CLOB) into l_doc_request from dual;

  l_content := '{"$contexte$":"'||l_contexte_request||'","$iddoc$":"'||iddoc||'","$doc$":"'||l_doc_request||'"}';
  url := 'http://'|| l_host_name || ':'|| l_port || '/indexationsolr/GetIndexationSolr';

  --SP_LOG('iddoc = '|| iddoc,'');
  --SP_LOG('url = '|| url,'');

  l_http_request :=
        UTL_HTTP.begin_request (url => url, method => 'POST', http_version => 'HTTP/1.1');

        UTL_HTTP.set_header (l_http_request, 'User-Agent', 'Mozilla/4.0');
        --UTL_HTTP.set_header (l_http_request,'Host',l_host_name || ':' || l_port);
        UTL_HTTP.set_header (l_http_request, 'Connection', 'close');
        --UTL_HTTP.set_header(l_http_request, 'Content-Type', 'text/xml;charset=UTF-8');
        UTL_HTTP.set_header(l_http_request, 'content-type', 'application/json');
        UTL_HTTP.set_header (l_http_request,'Content-Length', length (l_content));
        UTL_HTTP.set_header (l_http_request, 'Transfer-Encoding', 'chunked' );
        UTL_HTTP.set_body_charset('utf8');

        req_length := DBMS_LOB.getlength (l_content);
        offset :=1;
        bufferY :=null;
        amount:=4000;

        --SP_LOG('l_content = ', l_content);


  WHILE (offset < req_length)

        LOOP
            DBMS_LOB.read (l_content,amount,offset,bufferY);
            UTL_HTTP.write_text (l_http_request, bufferY);
            offset := offset + amount;
        END LOOP;

        l_http_response := UTL_HTTP.get_response (l_http_request);
        l_clob_response :=null;


        BEGIN

                <<response_loop>>
                LOOP
                    UTL_HTTP.read_raw (l_http_response, l_raw_data, l_buffer_size);
                    l_clob_response := l_clob_response || UTL_RAW.cast_to_varchar2 (l_raw_data);
                    DBMS_OUTPUT.PUT_LINE ( 'l_clob_response = '|| l_clob_response );
                    --SP_LOG( 'l_clob_response = ',l_clob_response );
                END LOOP response_loop;



                EXCEPTION
                WHEN UTL_HTTP.end_of_body THEN
                UTL_HTTP.end_response (l_http_response);
         END;

  IF l_http_request.private_hndl IS NOT NULL THEN
     UTL_HTTP.end_request (l_http_request);
  END IF;

  IF l_http_response.private_hndl IS NOT NULL THEN
     UTL_HTTP.end_response (l_http_response);
  END IF;

  EXCEPTION
  WHEN UTL_HTTP.request_failed THEN
    UTL_HTTP.end_response(l_http_response);
    --SP_LOG( 'exception1 = ',sqlerrm );
  WHEN others THEN
    UTL_HTTP.end_response (l_http_response);
    --SP_LOG( 'exception2 = ',sqlerrm );


END;