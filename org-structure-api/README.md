# Requirements

OrgStructure represents organizational structure of one or more companies.
OrgStructure implemented as tree of OrgItems that could be one of: OrgUnit and OrgPosition.
OrgUnit is a branch node that could represent company, department, division, unit and etc.
Root of OrgStructure contains ordered sequence of OrgUnits.
OrgPosition represents position in related organisation unit.
OrgUnit contains ordered sequence of OrgUnits and OrgPositions.
OrgItem (OrgUnit or OrgPosition) has the following attributes:
 * id - unique id 
 * name - name of OrgItem
 * shortName - short name of OrgItem
 * description - description (optional)
 * parent - id of parent OrgUnit
 * orgRoles - set of OrgRole assignments to OrgItem
 * attributeAssignments - map of OrgAttribute assignments to OrgItem

OrgUnit has the following attributes:
 * headId - OrgPosition id of head of OrgUnit (optional)
 * children - ordered sequence of OrgItems

OrgPosition has the following attributes:
 * personId - id of person assigned to OrgPosition

OrgRole represents organisational role of the OrgPosition. 
OrgRole could be assigned to OrgUnit so children of the OrgUnit inherit this OrgRole

OrgAttribute represents key-value labels that assigned to OrgItem.
OrgAttrubute is used to search OrgItems.
OrgAttribute could be assigned: 
 * directly to OrgItem, 
 * directly to OrgUnit with inheritance flag, so all children inheriths attribute assignment
 * indirectly to OrgItem with inherited flag, inherited from parent OrgUnit

OrgStructure has the following operations:
 * create OrgItem and attach it to specified parent and specified position (optional)
 * update OrgItem
 * deactivate OrgItem 
 * activate OrgItem 
 * change OrgItem order position
 * assign OrgRole to OrgItem
 * unassign OrgRole from OrgItem
 * assign OrgAttribute to OrgItem
 * unassign OrgAttribute to OrgItem
 * assign person to OrgPosition
 * unassign person to OrgPosition
 * assign head of OrgUnit
 * unassign head of OrgUnit
 * get OrgItem by id
 * get OrgItems by ids
 * find OrgItems by OrgItemFilter

OrgRole has the following operations:
 * create
 * update
 * deactivate
 * activate
 * get OrgRole by id
 * get OrgRoles by ids
 * find OrgRoles by OrgRoleFilter
