#!/bin/bash
mkdir curEnv \
  && cp -p ./envs/$ENV/* ./curEnv \
  && envsubst <kustomization.yaml > ./curEnv/kustomization.yaml \
  && cat <&0 > ./curEnv/all.yaml
kustomize build ./curEnv \
  && rm -Rf curEnv