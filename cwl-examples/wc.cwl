cwlVersion: cwl:v1.0
class: CommandLineTool
baseCommand: wc
inputs:
  - id: inputfile
    type: File
    inputBinding:
      position: 1
outputs:
  - id: outputfile
    type: stdout
stdout: $(inputs.inputfile.basename).wc
