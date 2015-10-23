# This resource configuration file describes the detailed configuration of the "instance" resource to be created by
# the Deployment Manager. It's a customisation of an "instance" resource type.
#
# references:
#   https://cloud.google.com/compute/docs/reference/latest/instances
#   https://cloud.google.com/deployment-manager/step-by-step-guide/create-a-template
#   https://cloud.google.com/deployment-manager/step-by-step-guide/using-python-templates
#   https://cloud.google.com/deployment-manager/step-by-step-guide/create-container-deployment


import yaml

COMPUTE_URL_BASE = 'https://www.googleapis.com/compute/v1/'


def GlobalComputeUrl(project, collection, name):
    return ''.join([COMPUTE_URL_BASE, 'projects/', project, '/global/', collection, '/', name])


def ZonalComputeUrl(project, zone, collection, name):
    return ''.join([COMPUTE_URL_BASE, 'projects/', project, '/zones/', zone, '/', collection, '/', name])


def GenerateConfig(context):
    """Generate configuration."""

    # Properties for the container-based instance.
    instance = {
        'zone': context.properties['instanceZone'],
        'machineType': ZonalComputeUrl(context.env['project'],
                                       context.properties['instanceZone'],
                                       'machineTypes',
                                       context.properties['instanceMachineType']),
        'metadata': {
            'items': [{
                'key': 'google-container-manifest',
                'value': context.imports[context.properties['containerManifest']]
            }]
        },
        'disks': [{
            'deviceName': 'boot',
            'type': 'PERSISTENT',
            'autoDelete': True,
            'boot': True,
            'diskSizeInGb': 10,
            'diskType': context.properties['instanceBootDiskType'],
            'initializeParams': {
                'diskName': context.properties['instanceName'] + '-disk',
                'sourceImage': GlobalComputeUrl('google-containers',
                                                'images',
                                                context.properties['instanceImage'])
            },
        }],
        'networkInterfaces': [{
            'accessConfigs': [{
                'name': 'external-nat',
                'type': 'ONE_TO_ONE_NAT'
            }],
            'network': GlobalComputeUrl(context.env['project'], 'networks', context.properties['instanceNetwork'])
        }]
    }

    # Resources to return.
    resources = {
        'resources': [{
            'name': context.properties['instanceName'],
            'type': 'compute.v1.instance',
            'properties': instance
        }]
    }

    return yaml.dump(resources)
