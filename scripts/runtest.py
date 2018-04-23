#!/usr/bin/env python3
# License: CC0

"""
Helper script that runs a single test.
"""

import argparse
import subprocess

from itertools import *

josm_env = '.:test/unit:test/functional:dist/josm-custom.jar:test/lib/commons-testing/*:test/lib/fest/*:test/lib/junit/*:test/lib/*:test/lib/unitils-core/*:tools/*:tools/spotbugs/*'

def main():
    parser = argparse.ArgumentParser(description = 'Run single JOSM test')
    parser.add_argument('testpath',
            help = 'Relative path to test. Example: ./test/unit/org/openstreetmap/josm/tools/GeometryTest.java')
    args = parser.parse_args()

    print('Compiling test')
    subprocess.check_output('javac -cp %s %s' % (josm_env, args.testpath), shell = True)

    # parse parh to tests and start with 'org' folder
    parts = list(dropwhile(lambda x: x != 'org', args.testpath.split('/')))
    parts[-1] = parts[-1].split('.')[0]
    class_name = '.'.join(parts)

    print('Running test')
    subprocess.run('java -cp %s org.junit.runner.JUnitCore %s' % (josm_env, class_name), shell = True)
    
main()
