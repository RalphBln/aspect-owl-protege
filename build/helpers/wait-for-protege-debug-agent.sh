#!/bin/sh

echo -n 'Waiting for Protégé remote debug agent on port 8765...' \
  && until nc -z 0.0.0.0 8765; \
  do sleep 1 && echo -n .; done \
  && echo '\nProtégé remote debug agent on port 8765 is now available!'