#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <openssl/ssl.h>
#include <openssl/err.h>
 
#define PORTNUM 2323
//https://www.cs.utah.edu/~swalton/listings/articles/ssl_server.c
int createSock(int port){
    int sockfd;
    struct sockaddr_in serv; /* socket info about our server */
    memset(&serv, 0, sizeof(serv));           /* zero the struct before filling the fields */
    serv.sin_family = AF_INET;                /* set the type of connection to TCP/IP */
    serv.sin_addr.s_addr = INADDR_ANY; /* set our address to any interface */
    serv.sin_port = htons(PORTNUM);           /* set the server port number */

    // bind serv information to mysocket
    bind(sockfd, (struct sockaddr *)&serv, sizeof(struct sockaddr));

    // start listening, allowing a queue of up to 1 pending connection
    listen(sockfd, 5);
    return sockfd;
}
//https://wiki.openssl.org/index.php/Simple_TLS_Server
void initOpenssl(){
    SSL_load_error_strings();	
    OpenSSL_add_ssl_algorithms();
}
void cleanupSSL(){
    EVP_cleanup();
}

SSL_CTX *createContext(){
    const SSL_METHOD *method;
    SSL_CTX *ctx;

    method = SSLv23_server_method();

    ctx = SSL_CTX_new(method);
    if (!ctx) {
	perror("Unable to create SSL context");
	ERR_print_errors_fp(stderr);
	exit(EXIT_FAILURE);
    }

    return ctx;
}

void configureContext(SSL_CTX *ctx)
{
    SSL_CTX_set_ecdh_auto(ctx, 1);

    /* Set the key and cert */
    if (SSL_CTX_use_certificate_file(ctx, "cert.pem", SSL_FILETYPE_PEM) < 0) {
        ERR_print_errors_fp(stderr);
	exit(EXIT_FAILURE);
    }

    if (SSL_CTX_use_PrivateKey_file(ctx, "key.pem", SSL_FILETYPE_PEM) < 0 ) {
        ERR_print_errors_fp(stderr);
	exit(EXIT_FAILURE);
    }
}

int main(int argc, char *argv[])
{
    char* msg = "Hello World haha!\n";
    SSL_CTX *ctx;
    int mysocket;
    socklen_t socksize = sizeof(struct sockaddr_in); 

    initOpenssl();

    ctx = createContext();
    configureContext(ctx);

    mysocket = createSock(PORTNUM);

    //connection recieved
    while(1)
    {
        struct sockaddr_in dest; /* socket info about the machine connecting to us */
	int consocket = accept(mysocket, (struct sockaddr *)&dest, &socksize);
	SSL *ssl;
	uint len = sizeof(dest);

	int client = accept(mysocket,(struct sockaddr*)&dest,&len);

        ssl = SSL_new(ctx);
        SSL_set_fd(ssl, client);
        if(SSL_accept(ssl) <=0){
	    ERR_print_errors_fp(stderr);	
	}else{
	    SSL_write(ssl,msg,strlen(msg));	
	}
	SSL_free(ssl);
	close(client);
    }
    close(mysocket);
    SSL_CTX_free(ctx);
    return EXIT_SUCCESS;
}
