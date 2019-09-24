cwlVersion: cwl:v1.0
class: CommandLineTool
baseCommand: tail
inputs:
  - id: numLines
    type: int?
    inputBinding:
      position: 1
      prefix: -n
  - id: file
    type: File
    inputBinding:
      position: 2
outputs:
  - id: outputfile
    type: stdout
stdout: $(inputs.file.basename).tail
