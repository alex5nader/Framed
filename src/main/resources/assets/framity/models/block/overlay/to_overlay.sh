#!/bin/sh

OUT=$(cat $1 | jq '
with_entries(
	if (.key == "elements") then (
		{key: "elements", value: .value | map (
			[., .]
		) | flatten}
	) else (
		.
	) end
)')
if [ $2 ]; then
	echo $OUT > $2
else
	echo $OUT > $1
fi
