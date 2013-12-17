# -*- coding: utf-8 -*-
import urllib2
import re
import sys
import os
import HTMLParser
import codecs

base_dir = sys.argv[1]
startint = 100000
BASE_URL='http://skyways.lib.ks.us/genweb/civilwar/'

links = [
'Obitiaries%20A_B.htm',
'Obitiaries%20C_E.htm',
'Obitiaries%20F_G.htm',
'Obitiaries%20H_J.htm',
'Obitiaries%20K_N.htm',
'Obitiaries_O_R.htm',
'Obitiaries_S.htm',
'Obitiaries_T_Z.htm'
]

raw_path = os.path.join(base_dir,'raw')
if not os.path.exists(raw_path):
	os.mkdir(raw_path)

parser = HTMLParser.HTMLParser()

for link in links:
	fh = urllib2.urlopen(BASE_URL+link)
	data = fh.read()
	fh.close()
	urls = [n for n in re.findall('href="(.*?)"', data, re.S | re.UNICODE) if n.find('civil_war_veterans') == -1]
	for url in urls:
		if not url.startswith("http://"):
			url = BASE_URL + url
		fh = urllib2.urlopen(url)
		data = fh.read().decode('Windows-1252').replace(u'\\xa0', u'').replace("\r", "").replace("\n", " ")
		fh.close()
		name = re.findall('<b><font size="5">(.*?)</font>', data, re.S)
		if len(name) == 0:
			print "Warning! Could not find name for "+url
			continue
		name = name[0]
		name = parser.unescape(name).strip()
		obit = re.findall('<b><font size="\d">&nbsp;(.*?)</font></b>', data, re.S | re.UNICODE)
		obit = [parser.unescape(o.strip()).replace("</o:p>", "").replace("</O:P>", "") for o in obit if not o.startswith("<o:p>") and len(o.strip()) > 0]
		if len(obit) == 0:
			print "Warning! No obit found for "+name
			continue
		path = os.path.join(raw_path, str(startint)+"_"+name.replace(" ", "_")+".txt")
		try:
			fh = codecs.open(path, 'w', 'utf-8')
		except:
			print "Warning! Error found writing "+name
			continue
		fh.write(name)
		fh.write("\n\n")
		print "Writing "+name
		for o in obit:
			fh.write(o)
			fh.write("\n")
		fh.close()
		startint += 1