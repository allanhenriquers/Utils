
PIDS=$(pgrep -f idea)

while kill -0 $PIDS ; do
    echo "Process is still active..."
    sleep 1
done;