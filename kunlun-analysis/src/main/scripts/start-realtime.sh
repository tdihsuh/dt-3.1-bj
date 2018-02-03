#!/bin/sh -
if [[ "$#" != "2" ]]; then
	echo "Usage: type startTime \
startTime: utc time, example: log_iis 2013-12-23T:16:00.000Z"
	exit 1
fi

home="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
$home/bin/kunlun.sh start realtime $@
