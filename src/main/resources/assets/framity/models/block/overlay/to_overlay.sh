#!/bin/sh

cat $1 | jq '
with_entries(
	if (.key == "elements") then (
		{key: "elements", value: .value | map (
			[., .]
		) | flatten}
	) else (
		.
	) end
)
'
