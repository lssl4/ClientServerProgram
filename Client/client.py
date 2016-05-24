import socket
import ssl
#https://carlo-hamalainen.net/blog/2013/1/24/python-ssl-socket-echo-test-with-self-signed-certificate
sslSock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
#sslSock = ssl.wrap_socket(s,ca_certs="serv.cert",cert_reqs=ssl.CERT_REQUIRED)
sslSock.connect(("127.0.0.1",2323))
sslSock.send(bytes("NOOOOOOOOOOOO - Darth Vader",'utf-8'))
sslSock.flush()
msg = str(sslSock.recvmsg(1024))
print(msg)
sslSock.close()
