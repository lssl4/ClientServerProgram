'''
Alexander Miller (21512714)
Tzi Siong Leong (20753794)
Trae Shaw (21521443)
'''
import socket
import ssl
import hashlib
from optparse import OptionParser
import os
import sys
#https://carlo-hamalainen.net/blog/2013/1/24/python-ssl-socket-echo-test-with-self-signed-certificate
#http://stackoverflow.com/questions/17695297/importing-the-private-key-public-certificate-pair-in-the-java-keystore
#https://docs.python.org/3/library/ssl.html
#https://docs.python.org/3/library/optparse.html
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
    #create a SSL Socket
    hostInfo = options.hostname.split(':')
    ctx = ssl.create_default_context(purpose=ssl.Purpose.SERVER_AUTH,cafile="serv.pem")
    ctx.check_hostname = False
    s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    sslSock = ctx.wrap_socket(s)
    sslSock.connect((hostInfo[0],int(hostInfo[1])))

    #deal with options selected
    #Options print if option was successful on server end to stdout
    if not(options.cert is None):
        f = open(options.cert,'r')
        print(str(os.path.getsize(options.cert)))
        sslSock.send("-u " + options.cert + "\n")
        sslSock.send(str(os.path.getsize(options.cert))+"\n")
        sslSock.sendall(f.read())
        msg = "";
        n = sslSock.read(1)
        while n != "\n":
            msg += n;
            n = sslSock.read(1)
        print >>sys.stderr, msg

    if not(options.valid is None):
        sslSock.send("-v " + options.valid[0] + "\n" + options.valid[1] + "\n")
        msg = "";
        n = sslSock.read(1)
        while n != "\n":
            msg += n;
            n = sslSock.read(1)
        print >>sys.stderr, msg

    if (options.namedCircle > 0):
        sslSock.send("-n "  + options.namedCircle + "\n")
        msg = "";
        n = sslSock.read(1)
        while n != "\n":
            msg += n;
            n = sslSock.read(1)
        print >>sys.stderr, msg

    if (options.circle > 0):
        sslSock.send("-c " + str(options.circle) + "\n")
        msg = "";
        n = sslSock.read(1)
        while n != "\n":
            msg += n;
            n = sslSock.read(1)
        print >>sys.stderr, msg

    if not(options.addfilename is None):
        f = open(options.addfilename,'r')
        sslSock.send("-a " + options.addfilename+"\n")
        sslSock.send(str(os.path.getsize(options.addfilename))+"\n")
        sslSock.sendall(f.read())
        msg = "";
        n = sslSock.read(1)
        while n != "\n":
            msg += n;
            n = sslSock.read(1)
        print >>sys.stderr, msg

    if not(options.fetchfile is None):
        sslSock.send("-f "+ options.fetchfile + "\n")
        leng = "";
        n = sslSock.read(1)
        while n != "\n":
            leng += n;
            n = sslSock.read(1)
        leng = int(leng)
        if(leng >=0):
            f = sslSock.read(leng)
            while(len(f)<leng):
                f += sslSock.read(leng-len(f))
            sys.stdout.write(f)
        else:
            print >>sys.stderr, "File Not Found"

    if (options.toList):
        data = "-l\n"
        sslSock.send(data)
        #read in number of lines
        lines = "";
        n = sslSock.read(1)
        while n != "\n":
            lines += n;
            n = sslSock.read(1)
        #reading file names now
        print("Files Found = " + lines)
        for x in range(0,int(lines)):
            m = ""
            c = sslSock.read(1)
            while c != "\n":
                m += c
                c = sslSock.read(1)
            print(m)
    sslSock.close()
