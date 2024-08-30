Built a basic HTTP Server supporting GET and POST methods

Has support for GZIP compression


Start the server:

Pass the absolute path by setting the directory flag, this is where you want the files to be saved and retrieved from, this will be used when '/files' endpoint is hit, as mentioned below.

--directory /{mydir}

Endpoints:

GET:

/ - Returns 200

/echo/{str} - Returns {str} embedded in the body

/user-agent - Returns the user agent present in the request made

/files/{name} - Returns the content of the file name present in the path {name}

/* - Any other GET requests except for the ones mentioned above returns 404


POST:

/files/{name} : Creates a file named {name} and Returns 201


Encoding:

Only supports gzip, add gzip as the accepted encoding scheme in the header and the data will be returned in gzip compressed form
