import socket
import ssl
import hashlib
from optparse import OptionParser
import os
import sys
#https://carlo-hamalainen.net/blog/2013/1/24/python-ssl-socket-echo-test-with-self-signed-certificate
#http://stackoverflow.com/questions/17695297/importing-the-private-key-public-certificate-pair-in-the-java-keystore
usage = "usage: %prog [options] arg1 arg2"
msgBuffer = 1024
parser = OptionParser(usage = usage,add_help_option=False)
parser.add_option("-p", "--help",action="store_true",dest="help",
                  help="show this help message")

parser.add_option("-h", "--host", dest="hostname",metavar="hostname:port",
                  help="provide the remote address hosting the oldtrusty server")

parser.add_option("-a", "--add", dest="addfilename",metavar="filename",
                  help="file to add or replace")

parser.add_option("-c", "--circle", dest="circle",default=0,metavar="number",
                  help="the required circumference (length) of a circle of trust")

parser.add_option("-f", "--fetch", dest="fetchfile",metavar="filename",
                  help="the file to fetch from oldtrusty")

parser.add_option("-l", "--list",action="store_true",dest="toList",default=False,
                  help="list all stored files and how they are protected")

parser.add_option("-n", "--name", dest="namedCircle",metavar="name",
                  help="require a circle of trust to involve the named person (i.e. their certificate)")

parser.add_option("-u", "--upload", dest="cert",metavar="certificate",
                  help="upload a certificate to the oldtrusty server")

parser.add_option("-v", "--validate", dest="valid",nargs=2,metavar="filename certificate",
                  help="provide the remote address hosting the oldtrusty server")

(options, args) = parser.parse_args()
if options.help:
    parser.print_help()
elif (options.hostname is None):
    print("hostname required")
    exit
else:  
    hostInfo = options.hostname.split(':')
    ctx = ssl.create_default_context(purpose=ssl.Purpose.SERVER_AUTH,cafile="serv.pem")
    ctx.check_hostname = False
    s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    sslSock = ctx.wrap_socket(s)
    sslSock.connect((hostInfo[0],int(hostInfo[1])))
    #sslSock.send("hi\n")

    if not(options.cert is None):
        sslSock.send("-u " + options.cert + "\n")
        sslSock.read(msgBuffer)
    if not(options.valid is None):
        sslSock.send("-v " + options.valid[0] + " " + options.valid[1] + "\n")
        sslSock.read(msgBuffer)
    if (options.namedCircle > 0):
        sslSock.send("-n "  + options.namedCircle + "\n")
        sslSock.read(msgBuffer)
    if (options.circle > 0):
        sslSock.send("-c " + str(options.circle) + "\n")
        msg = sslSock.read(msgBuffer)
        print >>sys.stderr, msg
    if not(options.addfilename is None):
        f = open(options.addfilename,'r')
        print(str(os.path.getsize(options.addfilename)))
        sslSock.send(str(os.path.getsize(options.addfilename))+"\n")
        sslSock.sendall(f.read())
        
    if not(options.fetchfile is None):
        sslSock.send("-f "+ options.fetchfile + "\n")
        leng = int(sslSock.read())
        f = sslSock.read(leng)
        while(len(f)<leng):
            f += sslSock.read(leng-len(f))
        sys.stdout.write(f)
        
    if (options.toList):
        data = "-l"
        sslSock.send(data)
        
    #sslSock.send("hi2\n")
    #msg = str(sslSock.read(24))
    #print(msg)
    sslSock.close()
