#! /usr/bin/env python

import os
import re
import sys

from os.path import dirname, join as joinpath, normpath

java_directories = ["src", "test"]

blurb = [
    "/*\n",
    " * JCarder -- cards Java programs to keep threads disentangled\n",
    " *\n",
    " * Copyright (C) 2006-2007 Enea AB\n",
    " * Copyright (C) 2007 Ulrik Svensson\n",
    " * Copyright (C) 2007 Joel Rosdahl\n",
    " *\n",
    " * This program is made available under the GNU GPL version 2. See the\n",
    " * accompanying file LICENSE.txt for details.\n",
    " *\n",
    " * This program is distributed in the hope that it will be useful, but"
    " WITHOUT\n",
    " * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY"
    " or\n",
    " * FITNESS FOR A PARTICULAR PURPOSE.\n",
    " */\n",
    "\n",
    ]

def update_blurb(path):
    found_code = False
    blurb_lines = []
    code_lines = []
    for line in open(path).readlines():
        if not found_code and not re.match(r"\s*(/|\*|$)", line):
            found_code = True
        if found_code:
            code_lines.append(line)
        else:
            blurb_lines.append(line)
    if blurb_lines != blurb:
        print "updating blurb in %r" % path
        tmp_path = "%s.tmp.%d" % (path, os.getpid())
        fp = open(tmp_path, "wb")  # Write binary since we don't want CR.
        fp.writelines(blurb)
        fp.writelines(code_lines)
        os.rename(tmp_path, path)

def main(argv):
    root = normpath(joinpath(dirname(argv[0]), ".."))
    for javadir in java_directories:
        for (dirpath, dirnames, filenames) in os.walk(joinpath(root, javadir)):
            for filename in filenames:
                if filename.endswith(".java"):
                    update_blurb(joinpath(dirpath, filename))

if __name__ == "__main__":
    main(sys.argv)
