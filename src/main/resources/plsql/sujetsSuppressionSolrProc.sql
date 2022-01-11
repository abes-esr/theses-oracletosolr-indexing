---------------------------------------------------
java1.8 tomcat8
---------------------------------------------------


create or replace PROCEDURE suppressionsolr(iddoc in number)
AS

l_http_request     UTL_HTTP.req;
l_http_response    UTL_HTTP.resp;
l_buffer_size      NUMBER (10)      := 512;
l_line_size        NUMBER (10)      := 50;
l_lines_count      NUMBER (10)      := 20;
l_int_request      NUMBER (10);
l_line             VARCHAR2 (32767);
l_substring_msg    VARCHAR2 (32767);
l_raw_data         RAW (512);
l_clob_response    CLOB;
l_host_name        VARCHAR2 (128)   := 'xxxxxxxxxxxxx.fr'; --Web Server URL
l_port             VARCHAR2 (128)   := '8100'; --Web Server port
req_length binary_integer;
bufferY   varchar(2000);
amount  pls_integer:=2000;
offset  pls_integer:=1;
l_contexte_request VARCHAR2 (128)   := 'sujets';
l_content varchar2(4000):=null;


BEGIN


  l_int_request := iddoc;
  l_content := '{"iddoc":"'||iddoc||'", "contexte":"'||l_contexte_request||'"}';
  DBMS_OUTPUT.PUT_LINE ( 'l_int_request : '|| iddoc);
  l_http_request :=
     UTL_HTTP.begin_request (url               =>    'http://'
                                                  || l_host_name
                                                  || ':'
                                                  || l_port
                                                  || '/indexationsolr/GetSuppressionSolr',
                             method            => 'POST',
                             http_version      => 'HTTP/1.1'
                            );
      UTL_HTTP.set_header (l_http_request, 'User-Agent', 'Mozilla/4.0');
      UTL_HTTP.set_header (l_http_request,'Host',l_host_name || ':' || l_port);
      UTL_HTTP.set_header (l_http_request, 'Connection', 'close');
      UTL_HTTP.set_header(l_http_request, 'Content-Type', 'application/json');
      UTL_HTTP.set_header (l_http_request,'Content-Length', length (l_content));

        req_length := DBMS_LOB.getlength (l_content);
        offset :=1;
        bufferY :=null;
        amount:=2000;

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
END;

---------------------------------------------------
java11 tomcat9
---------------------------------------------------


create or replace PROCEDURE suppressionsolr(iddoc in number)
AS

l_response clob;



BEGIN

    apex_web_service.g_request_headers(1).name := 'Content-Type';
    apex_web_service.g_request_headers(1).value := 'application/json';

    l_response := apex_web_service.make_rest_request(
        p_url => 'http://serveur:port/indexationsolr/GetSuppressionSolr?iddoc='||iddoc||'&contexte=sujets',
        p_http_method => 'GET');


    DBMS_OUTPUT.PUT_LINE(l_response);



END;

---------------------------------------------------
orpins-p
---------------------------------------------------


create or replace PROCEDURE suppressionsolr(iddoc in number)
AS

l_http_request     UTL_HTTP.req;
l_http_response    UTL_HTTP.resp;
l_buffer_size      NUMBER (10)      := 512;
l_line_size        NUMBER (10)      := 50;
l_lines_count      NUMBER (10)      := 20;
l_int_request      NUMBER (10);
l_line             VARCHAR2 (32767);
l_substring_msg    VARCHAR2 (32767);
l_raw_data         RAW (512);
l_clob_response    CLOB;
l_host_name        VARCHAR2 (128)   := 'xxxxxxxxxxxxxx.fr';
l_port             VARCHAR2 (128)   := 'xx';
req_length binary_integer;
bufferY   varchar(2000);
amount  pls_integer:=2000;
offset  pls_integer:=1;
l_contexte_request VARCHAR2 (128)   := 'sujets';
l_content varchar2(4000):=null;
url varchar2(2000);



BEGIN

  --SP_LOG('Beginning suppressionsolr proc','');


  l_int_request := iddoc;
  l_content := '{"iddoc":"'||iddoc||'", "contexte":"'||l_contexte_request||'"}';
  url := 'http://'|| l_host_name || ':'|| l_port || '/indexationsolr/GetSuppressionSolr';

  --SP_LOG('iddoc = '|| iddoc,'');
  --SP_LOG('url = '|| url,'');


  l_http_request :=
        UTL_HTTP.begin_request (url => url, method => 'POST',http_version => 'HTTP/1.1' );

        UTL_HTTP.set_header (l_http_request, 'User-Agent', 'Mozilla/4.0');
        --UTL_HTTP.set_header (l_http_request,'Host',l_host_name);
        UTL_HTTP.set_header (l_http_request, 'Connection', 'close');
        UTL_HTTP.set_header(l_http_request, 'content-type', 'application/json');
        UTL_HTTP.set_header (l_http_request,'Content-Length',LENGTH (l_content));

        req_length := DBMS_LOB.getlength (l_content);
        offset :=1;
        bufferY :=null;
        amount:=2000;

        --SP_LOG('l_content = ', l_content);


  WHILE (offset < req_length)

        LOOP
            DBMS_LOB.read (l_content, amount,offset,bufferY);
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
     --SP_LOG( 'l_http_response.private_hndl IS NOT NULL ',l_clob_response);
  END IF;
END;