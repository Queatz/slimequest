import subprocess
subprocess.call('sudo killall java', shell=True)
subprocess.call('git pull', shell=True)

pid = subprocess.Popen(['nohup ./gradlew server:run &'], shell=True).pid

print('Started ' + str(pid))

try:
        subprocess.call('tail -f nohup.out', shell=True)
except (KeyboardInterrupt, ProcessLookupError):
        print("\n\nur my bro!\n")