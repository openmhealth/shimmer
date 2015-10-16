# This resource configuration file describes the detailed configuration of the "instance" resource to be created by
# the Deployment Manager. It's a customisation of an "instance" resource type.
#
# references:
#   https://cloud.google.com/compute/docs/reference/latest/instances
#   https://cloud.google.com/deployment-manager/step-by-step-guide/create-a-template
#   https://cloud.google.com/deployment-manager/step-by-step-guide/using-python-templates
#   https://cloud.google.com/deployment-manager/step-by-step-guide/create-container-deployment


import yaml


def GenerateEmbeddableYaml(yaml_string):
    # Because YAML is a space delimited format, we need to be careful about
    # embedding one YAML document in another. This function takes in a string in
    # YAML format and produces an equivalent YAML representation that can be
    # inserted into arbitrary points of another YAML document. It does so by
    # printing the YAML string in a single line format. Consistent ordering of
    # the string is also guaranteed by using yaml.dump.
    yaml_object = yaml.load(yaml_string)
    dumped_yaml = yaml.dump(yaml_object, default_flow_style=True)
    return dumped_yaml


def GenerateConfig(context):
    # We need to specify the container manifest as a YAML string in the instance's
    # metadata. Generate a YAML string that can be inserted into the overall
    # document.
    manifest = GenerateEmbeddableYaml(
        context.imports[context.properties["containerManifest"]])

    return """
resources:
  - type: compute.v1.instance
    name: %(instanceName)s
    properties:
      zone: %(instanceZone)s
      machineType: zones/%(instanceZone)s/machineTypes/%(instanceMachineType)s
      metadata:
        items:
          - key: google-container-manifest
            value: "%(manifest)s"
      disks:
        - deviceName: boot
          type: PERSISTENT
          diskType: %(instanceBootDiskType)s
          diskSizeGb: 10
          boot: true
          autoDelete: true
          initializeParams:
            sourceImage: projects/google-containers/global/images/%(instanceImage)s
      networkInterfaces:
        - network: global/networks/%(instanceNetwork)s
          accessConfigs:
            - name: External NAT
              type: ONE_TO_ONE_NAT
""" % {"instanceName": context.properties["instanceName"],
       "instanceImage": context.properties["instanceImage"],
       "instanceNetwork": context.properties["instanceNetwork"],
       "instanceZone": context.properties["instanceZone"],
       "instanceBootDiskType": context.properties["instanceBootDiskType"],
       "instanceMachineType": context.properties["instanceMachineType"],
       "manifest": manifest}
