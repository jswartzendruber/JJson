#!/usr/bin/env bash

nix-shell --run "javac -Xlint:unchecked Json.java me/jjson/*.java && java Json"
