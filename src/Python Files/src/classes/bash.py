import subprocess

def runBashFile(shFile):
    cmd = 'sh %s' % shFile
    bashCmd = 'bash -c \"%s\" ' % cmd
    return subprocess.getoutput(bashCmd)