#!/bin/sh -
home="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
startStop=$1
shift
command=$1
shift

params=$@

. "$home/bin/func.sh"

case $command in
    forwarder)
	main="com.hansight.kunlun.collector.forwarder.Forwarder"
	;;
    agent)
	main="com.hansight.kunlun.collector.agent.Agent"
	;;
    realtime)
	main="com.hansight.kunlun.analysis.realtime.single.RTHandler"
	;;
esac
case  $startStop in
    start)
	check
	start
	;;
    stop)
	stop
	;;
esac
