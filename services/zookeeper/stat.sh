PORT=$1

echo stat |nc 127.0.0.1 $PORT |awk -F : '{gsub(/ */,"");if ($1 == "Received" || $1 == "Sent" || $1 == "Outstanding") print $2}'

