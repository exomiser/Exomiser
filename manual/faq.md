---
layout: page
title: FAQ
subtitle: Frequently Asked Questions
---

* TOC
{:toc}


#### Q: Want help?

Use the help command of the command-line:

```
java -jar exomiser-cli-7.2.1.jar --help
```

#### Q: I get java.lang.UnsupportedClassVersionError

If you get the following error message:

    Exception in thread "main" java.lang.UnsupportedClassVersionError:
    de/charite/compbio/exomiser/cli/Main : Unsupported major.minor version

You are running an older unsupported version of Java. Exomiser requires java version 8 or higher. This can be checked by running:
    
```
java -version
```

You should see something like this in response:
    
    java version "1.8.0_65"
    
versions lower than 1.8 (e.g. 1.5, 1.6 or 1.7) will not run exomiser so you will need to install the latest java version.
  
### Q: Zip file reported as too big or corrupted

If, when running unzip `exomiser-cli-7.2.1-distribution.zip`, you see the following:
     
    error:  Zip file too big (greater than 4294959102 bytes)
    Archive:  exomiser-cli-7.2.1-distribution.zip
    warning [exomiser-cli-7.2.1-distribution.zip]:  9940454202 extra bytes at beginning or within zipfile
      (attempting to process anyway)
    error [exomiser-cli-7.2.1-distribution.zip]:  start of central directory not found;
      zipfile corrupt.
      (please check that you have transferred or created the zipfile in the
      appropriate BINARY mode and that you have compiled UnZip properly)

  Check that your unzip version was compiled with LARGE_FILE_SUPPORT and ZIP64_SUPPORT. This is standard with UnZip 6.00 and can be checked by typing:

```
unzip -version
```

This shouldn't be an issue with more recent linux distributions. 
