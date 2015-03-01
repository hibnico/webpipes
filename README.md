Webpipes
========

Webpipes is a library to manage web resources like javascript and css files which need processing before being served. It manages processing like minimization, file merging, css generation from less, linters, jsx transformation. Resources can be managed as streams from source, or regularly checked cached resources, or to be generated with an Ant task.


# Deployment

## publish snapshots

- run `ant publish-snapshot`

## delete snapshots:

- go to https://oss.sonatype.org/index.html#view-repositories;snapshots~browsestorage~/org/hibnet/
- delete folder

## release:

- run `ant release`
- go to https://oss.sonatype.org/
- select the orghibnet-XXXX repo and click 'Release'
