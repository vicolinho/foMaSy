 Create table if not exists entity_structure(ent_struct_id int not null auto_increment, Primary Key(ent_struct_id), name varchar(500) unique,description varchar(1000), ent_type varchar(200), index(name));
CREATE TABLE IF NOT EXISTS entity_structure_version (
ent_struct_version_id int   auto_increment, 
ent_struct_id_fk int, 
date_from date not null default '1900-01-01', 
date_to date not null default '2099-01-01', 
foreign key(ent_struct_id_fk) references entity_structure (ent_struct_id), 
primary key(ent_struct_version_id)
);
create table if not exists entity(
ent_id int auto_increment,
ent_struct_id_fk int ,
accession varchar(300) not null unique,
ent_type varchar (200) not null default 'entity',
date_from date not null default '1900-01-01',
date_to date not null default '2099-01-01',
foreign key (ent_struct_id_fk) references entity_structure (ent_struct_id),
Index (accession,ent_type),
Index (date_from),
Index (date_to),
Index (ent_struct_id_fk),
primary key (ent_id)
);
create table if not exists property(
prop_id int  auto_increment,
prop_name varchar(200),
lang varchar(10) default 'N/A',
scope varchar(100) default 'N/A',
datatype varchar (100) default 'string',
primary key (prop_id)
);
create table if not exists property_value(
prop_value_id int auto_increment,
prop_id_fk int,
ent_id_fk int,
prop_value varchar(4000),
from_date date not null default '1900-01-01',
to_date date not null default '2099-01-01',
foreign key (prop_id_fk) references property (property_id),
foreign key (ent_id_fk) references entity(entity_id),
index (from_date),
index(to_date),
index(ent_id_fk,prop_id_fk),
primary key  (prop_value_id)
);
Create table if not exists property_keyword(
keyword_id int  auto_increment,
keyword varchar(700),
kw_type enum('token','nameEntity') default 'token',
primary key (keyword_id)
);
create table if not exists property_value_part(
prop_value_id_fk int,
keyword_id_fk int,
foreign key (prop_value_id_fk) references property_value(prop_value_id),
foreign key (keyword_id_fk) references property_keyword(keyword_id),
primary key  (prop_value_id_fk,keyword_id_fk)
);
create table if not exists syn_set(
syn_set_id int,
keyword_id_fk int ,
primary key  (syn_set_id,keyword_id_fk),
foreign key (keyword_id_fk) references property_keyword(keyword_id)
);

Create table if not exists term_group(
token varchar(100), 
termgroup_id int,
index using btree(token,termgroup_id)
);
Create table if not exists termgroup_annotation (
	termgroup_id int ,
	annotating_ent_id_fk int,
	index(termgroup_id),
	foreign key (annotating_ent_id_fk) references entity(ent_id) 
);

create table if not exists rel_type (
rel_type_id int auto_increment ,
rel_name varchar (50),
is_directed boolean default true,
 primary key (rel_type_id)
);
create table if not exists entity_relationship(
src_id int not null,
target_id int not null,
ent_struct_id_fk int not null,
rel_type_id_fk int not null,
from_date date default '1900-01-01',
to_date date default '2099-01-01',
primary key (src_id,target_id,rel_type_id_fk),
foreign key (src_id) references entity (ent_id),
foreign key (target_id) references entity (ent_id),
foreign key (ent_struct_id_fk) references entity_structure(ent_struct_id),
foreign key (rel_type_id_fk) references rel_type(rel_type_id)
);

Create table if not exists annotations(
mapping_id_fk int,
src_entity_id int,
target_entity_id int,
similarity float,
is_verified boolean default false,
foreign key (src_entity_id) references entity,
foreign key (target_entity_id) references entity,
foreign key (mapping_id_fk) references annotation_mapping,
Primary key (mapping_id_fk, src_entity_id, target_entity_id)
);

Create table  if not  exists annotation_mapping (
mapping_id int auto_increment,
src_ent_struct_version_id_fk int ,
target_ent_struct_version_id_fk int,
name varchar(500)  not null,
foreign key (src_ent_struct_version_id_fk) references entity_structure_version, 
foreign key (target_ent_struct_version_id_fk) references entity_structure_version, 
method varchar(200),
index(src_ent_struct_version_id_fk,target_ent_struct_version_id_fk),
primary key (mapping_id) 
);

create table if not exists tmp_entity(
accession varchar(300),
ent_type varchar(200) not null default 'entity',
primary key(accession)
);
create table if not exists tmp_del_entity(
  accession varchar(300) not null unique,
  primary key(accession)
);
create table if not exists tmp_added_entity(
  accession varchar(300) ,
  primary key(accession)
);
create table if not exists tmp_relationship(
par_relationship varchar(300),
child_relationship varchar(300),
rel_type varchar(200) not null default 'is_a',
is_directed boolean,
primary key (par_relationship,child_relationship,rel_type)
);
create table if not exists tmp_added_relationship(
par_relationship varchar(300),
child_relationship varchar(300),
rel_type varchar(200) not null default 'is_a',
is_directed boolean,
primary key (par_relationship,child_relationship,rel_type)
);
create table if not exists tmp_properties(
accession_fk varchar(300),
prop_name varchar(200) not null,
lang varchar (10) default 'N/A',
scope varchar (100)  default 'N/A',
datatype varchar (100) default 'string',
prop_value varchar (4000)
);
create table if not exists tmp_added_properties(
accession_fk varchar (300),
prop_name varchar(200),
lang varchar (10),
scope varchar (100),
datatype varchar (100),
prop_value varchar (4000),
from_date date
);
Create table if not exists annotation_cluster_structure (
cluster_structure_id int primary key auto_increment,
name varchar(250) unique
);

create table if not exists annotation_cluster (
cluster_id int ,
 cluster_structure_id_fk int,
Primary key (cluster_id,cluster_structure_id_fk),
foreign key (cluster_structure_id_fk) references annotation_cluster_structure (cluster_structure_id)
);
create table if not exists annotation_cluster_elem (
cluster_id_fk int,
cluster_structure_id_fk int,
entity_id_fk int,
Primary key (cluster_id_fk, cluster_structure_id_fk, entity_id_fk),
foreign key (cluster_id_fk,cluster_structure_id_fk) references annotation_cluster (cluster_id,cluster_structure_id_fk),
foreign key (entity_id_fk) references entity (ent_id)
);

Create table if not exists  annotation_cluster_representant (
cluster_id_fk int, 
 cluster_structure_id_fk int,
 representant_id int primary key auto_increment,
 key(cluster_id_fk,cluster_structure_id_fk)
);




