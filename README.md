# cmd-geonetwork-client

A java client to import, export records, users and groups from a geonetwork instance.

This tool has been funded by Metagis (https://metagis.se/).

# Build

```
mvn install package
```

In `target` folder it's created a jar file that includes all the dependencies embedded: `cmd-geonetwork-client-jar-with-dependencies.jar`.

# Configuration file

## General properties

The tool requires a properties file with the following properties:

- `gn_user_name`: GeoNetwork user name used to export/import the information
- `gn_user_password`: GeoNetwork user password
- `gn_base_url`: Url of the GeoNetwork instance where the data should be exported/imported
- `output_path`: Path to export/import the data

If using custom authentication the following properties should be defined:

- `gn_login_service`: no need to change it
- `do_custom_authentication`: change it to the value `true`
- `custom_authentication_url`: url of the custom authentication service

## Properties for export mode

For export mode the following properties can be configured (that are not required in import mode)

- `gn_export_metadata_types` (optional): Comma separated list of metadata types to export. The values allowed are: `dataset`, `series `, `service`, `grouplyr`. If no value is configured all metadata types are exported.
- `gn_export_metadata_modified_since` (optional): Filter to retrieve metadata modified since the date specified. Format: `VALUE:UNITS`. Examples: 5:MINUTES (5 minutes ago), 2:HOURS (2 hours ago). Units supported: `MINUTES`, `HOURS`, `DAYS`, `MONTHS`, `YEARS`.
- `gn_export_filter_by_group_locally`: Flag to use when exporting from some old GeoNetwork instances as CSW filter by group doesn't work. Process all metadata retrieving for each one the group.
- `gn_request_delay`: Delay in milliseconds for each request, to avoid causing connection issues with the server.

## Properties for import mode

For import mode the following properties can be configured (that are not required in export mode):

- `gn_users_default_password`: Default password for new users in import process
- `gn_import_local_xslt` (optional): Path of local xslt to apply to imported metadata
- `gn_import_remote_xslt` (optional): Name of remote xslt (available in GeoNetwork) to apply to imported metadata

Additionally when importing a folder of plain metadata xml (not created with an export from this tool) the following properties are also required:

- `gn_default_import_user`: Owner user to assign to the imported metadata
- `gn_default_import_group`: Owner group to assign to the imported metadata
- `gn_default_import_status`: Metadata status to assign to the imported metadata
- `gn_default_import_privileges`: Format `GROUPNAME:priv1#...#;GROUPNAME#priv1#...` (Sample Group:publish#download#editing#notify#interactivemap#featured;all:publish)


## Examples 

### Example of file for export

```
# GeoNetwork credentials
gn_user_name=admin
gn_user_password=admin

# GeoNetwork url
gn_base_url= http://localhost:9090/geonetwork

gn_login_service=srv/eng/shib.user.login.noforward
do_custom_authentication=false
custom_authentication_url=http://idp/authentication/SessionController

# Output path for export/import data
output_path=/tmp/output

# Comma separated list of metadata types to export: dataset, series, service, grouplyr. Set empty to don't apply the filter.
gn_export_metadata_types=dataset

# Filter to retrieve metadata modified since a date. Set empty to don't apply the filter.
# Format: VALUE:UNITS. Examples: 5:MINUTES (5 minutes ago), 2:HOURS (2 hours ago). Units supported: MINUTES,HOURS,DAYS,MONTHS,YEARS
gn_export_metadata_modified_since=2:DAYS

# Export flag for old GeoNetwork instances not supporting CSW filter by group
gn_export_filter_by_group_locally=false

# Delay in ms to execute each request
gn_request_delay=1000
```

### Example of file for import

```
# GeoNetwork credentials
gn_user_name=admin
gn_user_password=admin

# GeoNetwork url
gn_base_url= http://localhost:9090/geonetwork

gn_login_service=srv/eng/shib.user.login.noforward
do_custom_authentication=false
custom_authentication_url=http://idp/authentication/SessionController

# Output path for export/import data
output_path=/tmp/output


# Default password for new users in import process
gn_users_default_password=dummypassword

# Path of local xslt to apply to imported metadata
#gn_import_xslt=./import.xsl
gn_import_local_xslt=

# Name of remote xslt (available in GeoNetwork) to apply to imported metadata
#gn_import_remote_xslt=convertV3.1To4.0.xsl
gn_import_remote_xslt=

# Default information to assign to the metadata if there's no XXXX_info.xml for the metadata
gn_default_import_user=LST_AB_data
gn_default_import_group=LST_AB_data
gn_default_import_status=Approved
# Format GROUPNAME:priv1#...#;GROUPNAME#priv1#... (Sample Group:publish#download#editing#notify#interactivemap#featured;all:publish)
gn_default_import_privileges=all:publish

```

# Show help

```
java -jar cmd-geonetwork-client-jar-with-dependencies.jar -h
usage: Cli
 -c,--config <arg>   configuration file
 -e,--export <arg>   Export mode
 -h,--help           Show help
 -i,--import         Import mode
 -r,--remove         Remove all metadata AND non-default users and groups
                     in import

```

# Export

```
java -jar cmd-geonetwork-client-jar-with-dependencies.jar -e -c ./cmd-geonetwork-client-export.properties
```

The tool creates in the `output_path` the following files/folders:

- users.json
- groups.json
- metadata
    - group_XXXX1
        uuid1.xml
        uuid1_info.xml
        uuid2.xml
        uuid2_info.xml
        
    - group_XXXX2
        uuid3.xml
        uuid3_info.xml
       
The `metadata` folder contains a subfolder with the metadata owned by each group.        

The `uuid_info.xml` files contain information about metadata: owner info, privileges, status, etc.

The parameter `-e` accepts an argument:

- `users`: exports only users
- `group`: exports only groups
- `all`: exports users, groups and metadata.


## Configuration of GeoNetwork services in 2.10 version

GeoNetwork 2.10 lacks of some services required, to get working the export add the following service in `WEB-INF\config.xml` in the `services` section:

```
<service name="xml.user.get">
  <class name=".services.user.Get" />
</service>
```

In `WEB-INF/config-metadata.xml` add the following service in the `services` section:

```
<service name="xml.metadata.admin.get">
  <class name=".services.metadata.GetAdminOper"/>
</service>
```
        
Update `WEB-INF/config-security-mapping.xml` with the 2 following entries, before the last entry to deny all other requests (the line is added in the snippet for reference, but should not be added, it's already in the file):

```
  <sec:intercept-url pattern="/srv/[a-z]{2,3}/xml.user.get!?.*" access="hasRole('UserAdmin')"></sec:intercept-url>
  <sec:intercept-url pattern="/srv/[a-z]{2,3}/xml.metadata.admin.get!?.*" access="hasRole('Editor')"></sec:intercept-url>
  
  ...
  
  <sec:intercept-url pattern="/.*" access="denyAll"></sec:intercept-url>
```
   
A restart of GeoNetwork is required.


# Import


```
java -jar cmd-geonetwork-client-jar-with-dependencies.jar -i -c ./cmd-geonetwork-client-import.properties
```

With the `-r` option the users (except Administrator profile), group (except internal groups) and related metadata is removed. In this mode the user is prompted for confirmation:

```
java -jar cmd-geonetwork-client-jar-with-dependencies.jar -i -c ./cmd-geonetwork-client-import.properties -r
You have selected the option delete all metadata and non default users and groups. Do you want to proceed? [y/N]: 
```

