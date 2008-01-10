#! /usr/bin/env python

import re
import sys

from os import mkdir
from os.path import dirname, exists, join as joinpath
from urllib2 import urlopen

urlbase = "http://jcarder.org/3pp/"

def get_3pps(path):
    jars = []
    for line in open(path):
        m = re.search('kind="lib" path="3pp/([^"]+)"', line)
        if m:
            jars.append(m.group(1))
    return jars

def get_url(jar):
    m = re.search('(.*)\-(.+)\.jar', jar)
    if m:
        return urlbase + m.group(1) + "/" + m.group(2) + "/" + jar
    else:
        return urlbase + jar

def download_file(url, dest_path):
    source_fp = urlopen(url)
    dest_fp = open(dest_path, "wb")
    while True:
        data = source_fp.read(2 ** 13)
        if not data:
            break
        dest_fp.write(data)
    dest_fp.close()

def main(argv):
    dest_dir = joinpath(dirname(argv[0]), "..", "3pp")
    if not exists(dest_dir):
        mkdir(dest_dir)
    classpath_file = joinpath(dirname(argv[0]), "..", ".classpath")
    for jar in get_3pps(classpath_file):
        jar_path = joinpath(dest_dir, jar)
        if not exists(jar_path):
            url = get_url(jar)
            print "Downloading %s" % url
            download_file(url, jar_path)

if __name__ == "__main__":
    main(sys.argv)
