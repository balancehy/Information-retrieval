# -*- coding: utf-8 -*-


import json
# if you are using python 3, you should 
#import urllib.request 
import urllib.request

#spechar=["+","-","&&","||","!","(",")","{","}","[","]","^","\"","~","*","?",":","/"]
queries={"id":[],"q":[]} # store all queries and query ids
corename="IRproject3"
numreturn=20
with open("queries.txt",'r') as f:
    lines=f.readlines()
    for line in lines:
        newline=line.rstrip("\n")  # stip line enders
        word=newline.split(' ',1) # separate query id and contents
        queries["id"].append(word[0])
        queries["q"].append(word[1])
f.close()
#print(queries)
# change query id and IRModel name accordingly

IRModel='default'
outfn = 'response.txt'
def getQuerystring(qu):
    quurl=urllib.parse.quote(qu)
    return "(text_en:("+quurl+"))"+"%20OR%20"+"(text_de:("+quurl+"))"+"%20OR%20"+"(text_ru:("+quurl+"))"
print(getQuerystring("whatisthis"))
# change the url according to your own corename and query
with open(outfn, 'w') as outf:
    for qid,qu in zip(queries["id"],queries["q"]):
        query=getQuerystring(qu) # one query string
        inurl = "http://localhost:8983/solr/"+corename+"/select?q="+query+"&fl=id%2Cscore&wt=json&indent=true&rows="+str(numreturn)
        print(inurl)
        #print(type(data.read().decode('utf-8')))# urlopen() generates a http response object, it has read() method to get the contents as bytes type. It can be converted to string using decode('utf-8')
        data = json.loads(urllib.request.urlopen(inurl).read())
        docs = data['response']['docs']
        print("The total number of found documents: ",data["response"]["numFound"])
        #print(docs)
        # the ranking should start from 1 and increase
        rank = 1
        for doc in docs:
            outf.write(qid + ' ' + 'Q0' + ' ' + str(doc['id']) + ' ' + str(rank) + ' ' + str(doc['score']) + ' ' + IRModel + '\n')
            rank += 1
outf.close()
#test=urllib.parse.quote("(PM Medvedev’s delegation to coordinate anti-terrorist actions)")
#inurl = "http://localhost:8983/solr/"+corename+"/select?q=text_en:"+test+"&fl=id%2Cscore&wt=json&indent=true&rows="+str(numreturn)
#print(inurl)
#urllib.request.urlopen(inurl)

