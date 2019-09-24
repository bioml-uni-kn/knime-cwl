#!/usr/bin/env cwl-runner

cwlVersion: v1.0
class: CommandLineTool
id: de.unikn.TarUnionTest
baseCommand: [tar, --extract]
inputs:
  tarfile:
    type: ["File", "string", "null"]
    inputBinding:
      prefix: --file
outputs:
  example_out:
    type: File
    outputBinding:
      glob: hello.txt
