### 9ci Organization structure into Org Types:


Organization data is sorted in a hierarchical arrangement, according to dimensions and measures. **Dimensions** (_Org_) group the data along natural categories and consist of one or more levels (org types). Each **level** (_Org Type_) represents a different grouping within the same dimension. For example, an org dimension can include levels (_org types_) such as customer, division, branch, business.

**Levels** (_org types_) are organized into one or more **hierarchies** we call _dimension paths_, typically from a coarse-grained level (for example, Year) down to the most detailed one (for example, Day). In 9ci structure the lowest would be custAccount and customer and the highest business, company and client). The individual category values (for example, Customer(205)) are called **members** (_org record_ , _set of data_).

Each combination of the dimension path values is called a **member** which is essentially row in the list. Some examples of members:
- CustAccount(20).branch(31)
- Customer(11).division(41).business(52)

A dimension can also have multiple _paths_ to provide different sequences of groupings. For example, a CustAccount dimension can have a custAccount path with branch.division.business and a operation path: opsDivision.sales.

Not all categorical data attributes need to become a member of a hierarchy level. Some grouping information is needed only as additional information for a member or for applying subsets to data. These attributes can be loaded into member properties. Member properties can be associated with any level in a hierarchy.
Validation for 9ci orgs can be configiured and you can specify which org members are required on each level (_OrgType_). For example, orgs that is not required can be factory or brand, not every Org needs to have a factory, or for other clients it can be called differently. 9ci stores them as flexOrg1, flexOrg2.


***Org Types settings***

**OrgType table**
-  **active flag** Active org types in base data: customer, company and client.
- TBD - user should be able to specify name of org type, so either code around Ids or have a custom name columns

**AppSetupConfig sets dimension paths**, organized list of members in the path (or paths).

The simplest and the most common installation is using only base data and single company. For example Mouser (look below) **Each dimension will have company and client on the top of the tree.** so we don't specify it in the config.

Others, like AAP or Delta can have multi company so data is segmented by company. This doesn't change dimension logic much, because company is required field and it doesn't sit on related table.

Optional , not required org types like factory or brand (Kolcraft or Revlon) are not in dimension paths.

The most complex is CED installation where we can have multi dimension and we have users logging in from different org type dimension levels. We need to keep data secured for their level.

**Examples by client**

```
/*Mouser   --------  OrgType active: customer,  company, client */
dimensions {
}

/*Delta and AAP  -------- OrgType active: customer, company, client. They have APPParam.multicompany=true */
dimensions {
}

/*Kolcraft   --------  OrgType active: customer, sales, factory, company, client */
dimensions {
}

/*Revlon   --------  OrgType active: customer, sales, brand, factory, business,company, client */
dimensions {
	org {
		default { 	
			path = customer.business	
		}
	}
}

/*CED   -------- OrgType active: customer, branch, divison, sales, business, region, company, client */
dimensionList {
//see below
}

```

**CED Use Case**

We should have one centralized utility service and methods with cached list of org types (children , parents, immediate parents) from dimension path based on user's org's org type. Example for CED:
```
dimensions {
	org {
		//these are the names, there will be 0 or more of these
		primary { 	
			path = custAccount.branch.division.business	
		}
		sales-ops {
			path = custAccount.branch.sales.region
		}
		custDim {
			path = custAccount.customer.division.business
		}
	}
	arTran{ //possible future use
	} etc...
}
```

Org type hierarchy tree from paths

```
   Default Company (but could be bad-debt and child levels can cross companies based on customer's company)
    |¹         |¹
    |          |
    |*         |*
 business   region
    |¹         |¹
    |          |
    |*         |*
 division    sales
 |¹ |¹           |¹
 |  |            |
 |  +-* branch *-+
 |        |¹
 |        +--------+
 |                 |
 |*                |*
customer ¹----* custAccount
  |*
  |
  |¹
company(default or bad-debt)
always assigned at the customer level no matter what the paths say.
```
**primary dimension**
business.division.branch.custAccount
```
CED Corp
├── ABC Biz (id:12)  <-business
│   ├── Mid Texas Div (id:22)  <-division
│   │   ├──Dallas Branch (id:32)  <-branch
│   │   │   └──Bobs Job (id:100)  <-Cust Account
│   │   +──Huston Branch (id:32) <-branch
│   +── Midwest Div (id:23) <-division
└── XYZ Biz (id:12) <-business
    +── etc...
```    
Org Members from above
- BC Biz {id:12} = null
- Mid Texas Div {id:22} = {businessId:12}
- Dallas Branch {id:32} = {businessId:12 , divisionId:22} //business is denormalized from division
- Bobs Job {id:100} = {businessId:12 , divisionId:22, branchId:32} //business and Div is denormalized from branch

**sales-ops dimension**
region.sales.branch.custAccount

```
ops dimension
├── West Region (id:13) / <-region
│   ├── Mountain Op Div (id:24) / <-sales
│   │   ├──Dallas Branch (id:32) / <-branch
│   │   │   └──Bobs Job (id:100) / <-Cust Account
│   │   +──Huston Branch (id:32) / <-branch
│   +── Midwest Op Div (id:25) / <-sales
├── Can Region (id:12) / <-region
│   +── etc...
```

Org Members from above
- West region (id:13)) will be null
- Mountain Ops Div (id:24) = {regionId:13}
- Dallas Branch (id:32) = {regionId:13 , salesId:24}
- Bobs Job (id:100) = {regionId:13 , salesId:24, branchId:32} //denormalized from branch

**customer dimension**
business.division.customer.custAccount
```
custDim dimension/
├── ABC Biz (id:12) / <-business
│   ├── Mid Texas Div (id:22) / <-division
│   │   ├──Customer Bob Corp (id:99) / <-customer
│   │   │   └──Bob Job 1 (id:100) / <-Cust Account
│   │   +──Customer Joe (id:98) / <-customer
│   +── Midwest Div (id:23) / <-division
└── XYZ Biz (id:12) / <-business
    +── etc...
```    
Org Members from above
- BC Biz (id:12) will be null
- Mid Texas Div (id:22) = {businessId:12}
- Customer Bob Corp (id:99) = {businessId:12 , divisionId:22} //business is denormalized from division
- Bob Job 1 (id:100) = {businessId:12 , divisionId:22} //business and Div is denormalized from branch

**Merged all together the Org Member rows will look like**
- **BC Biz (id:12)** will be null
- **Mid Texas Div (id:22)** = {businessId:12}
- **Customer Bob Corp (id:99)** = {businessId:12 , divisionId:22} //business is denormalized from division
- **Mountain Op Div (id:24)** = {regionId:13}
- **Dallas Branch (id:32)** = {businessId:12 , divisionId:22, regionId:13 , salesId:24}
- **Bob Job 1 (id:100)** = {businessId:12, divisionId:22, branchId:32, regionId:13 , salesId:24}

**Ideas for utility service by Org or OrgType**

- custAccount - look at 3 dimension paths and see where the custAccount exists:
  - getChildLevels: none
  - getParentLevels (getRequiredLevels): branch, customer, division, business, sales, region
  - getImmediateParentLevels: branch, customer
- customer -look at 3 dimension paths and see where the customer exists:
  - getChildLevels: custAccount
  - getParentLevels (getRequiredLevels): division, business
  - getImmediateParentMembers: division
- branch - look at 3 dimension paths and see where the branch exists:
  - getChildLevels: custAccount
  - getParentLevels (getRequiredLevels): division, business, sales, region
  - getImmediateParentMembers: division, sales
- division - look at 3 dimension paths and see where the division exists:
  - getChildLevels: branch, custAccount, customer
  - getParentLevels (getRequiredLevels): business
  - getImmediateParentMember: business
- business - look at 3 dimension paths and see where the business exists:
  - getChildLevels: branch, custAccount, customer, division
  - getParentLevels (getRequiredLevels): none
  - getImmediateParentMembers: none
- sales - look at 3 dimension paths and see where the sales exists:
  - getChildLevels: branch, custAccount
  - getParentLevels (getRequiredLevels): region
  - getImmediateParentMembers: region
- region - look at 3 dimension paths and see where the region exists:
  - getChildLevels: opsDivision, branch, custAccount
  - getParentLevels (getRequiredLevels): none
  - getImmediateParentMembers: none
- company -- look for company doc https://github.com/9ci/9ci/blob/master/documentation/SpecsAndDesigns/CompanyIdFilter.md
- client -- no users will be under client any more, but if anything all org types are children


**Validation for required fields in orgMember and arTranOrgMember are in OrgType**

For data query **we should always rely on arTranRelated and orgRelated** .

Required members for the given org type are members above your org type in any of the dimension paths.
OrgDimensionService.getParentLevels are required members .

	****For example CED:****

	OrgTypes:
	- custAccount - branch, division, business, sales, region
	- customer - division, business
	- branch - division, business, sales, region
	- division - business
	- sales - region
	- business
	- region

Validation on arTranOrgMember should take required fields either from orgMember for custAccount if active or customer if custAccount is inactive.


### functionality and what to use:
- what filters display on transaction, customer , custAccount filters - use AppSetupConfig for search forms with combination of OrgDimensionService.getChildLevels based on logged in org type level
- create user form - what org type list - use OrgDimensionService.getChildLevels
- access data per user's org type - use OrgDimensionService.getChildLevels . For Customer screen if current user's org type doesn't have customer in his path then use custAccount. For example branch is not anywhere in the path with customer so use custAccount.customer. Division has customer in the path so no problem.
- create forms for Orgs - use OrgDimensionService.getImmidiateParentMembers  - on create form for orgs we should required to enter only immiduate parent, and then in the background populate other parentMembers from it.

