# java-idxf
Image Displayer eXternal Frame (idxf) is a idd client which call upon an external image viewer in order to display the images.

## idxf shell script
```
#!/bin/bash
java -jar idxf-0.0.1-SNAPSHOT.jar "$@"
```

## Usage
### eog
`idxf -c "eog -f -w"`

### eom
~~`idxf -c "eom -f"`~~

### fbi
`sudo idxf -s -c "fbi -a -T 3"`

### feh
`idxf -s -c "feh -Z -F -Y -R 2"`

### fim
~~`idxf -c "fim -a"`~~

### xviewer
`idxf -c "xviewer -f -w"`
