#!/bin/bash
npm install;
for i in `seq 1 $1`; do
	pm2 start -f device.js;
done;