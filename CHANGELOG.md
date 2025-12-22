### v7.3.96

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.95...v7.3.96)
- release [link](https://github.com/yakworks/gorm-tools/commit/2df57c614fc1e7ab6d0a6f5471e4f168273aee48)
- Merge pull request #958 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/91b284d73bc76e68e4f1de6d66fc3aca5579c1df)
- Fix mail message maxSize - set 2.5MB (#956) [link](https://github.com/yakworks/gorm-tools/commit/6c5170b360d43ebb5733a3afde07696f9897abc1)
- MailMessage remove maxSize (#955) [link](https://github.com/yakworks/gorm-tools/commit/4b90425cee6a5f20fbe0c44c2309dcd885febb9c)
- Refactor role permissions (#954) [link](https://github.com/yakworks/gorm-tools/commit/820ea6a225f085a395a7c1809493731338fc26aa)

### v7.3.95

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.94...v7.3.95)
- release 7.3.95 [link](https://github.com/yakworks/gorm-tools/commit/19c08f234413e437e17a29b274a84b93d6b679ca)
- PartitionOrg lookup (#951) [link](https://github.com/yakworks/gorm-tools/commit/b683cca5bd1ba9bd83480f0cf4c9e37ad532f2c4)

### v7.3.94

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.93...v7.3.94)
- release [link](https://github.com/yakworks/gorm-tools/commit/397108262012d93db886edc87a5df1a957e70d15)
- Merge pull request #949 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/9dc0bd5adc04152523e9d62859cc36b82bccc1c0)
- Add asserts for wildcard permissions (#950) [link](https://github.com/yakworks/gorm-tools/commit/c1e7d29f811ba17ae04542bc8f68fa9373317fda)
- Fix yakworks.security.password config key (#945) [link](https://github.com/yakworks/gorm-tools/commit/80e07bbc043acc0fd2ad56b8944b63c620838b1c)
- Permission manager fix for : when permissionsEnabled is false. (#946) [link](https://github.com/yakworks/gorm-tools/commit/9c628e757148135faece0115f228331831f149f8)
- cleanup and add old DefaultSecurityConfiguration.applyOauthJwt [link](https://github.com/yakworks/gorm-tools/commit/329e3ccfb6b8a6d030068dec4bf16fd79c054b7d)
- Syncjob cleanup  (#941) [link](https://github.com/yakworks/gorm-tools/commit/ee53f6b67cc4f543e2301db3fbd3651861ab47b2)
- Jwt authority converter (#944) [link](https://github.com/yakworks/gorm-tools/commit/83526fd49c18a368eb9df18df21faeff06592210)
- ACL/Permissions  POC (#933) [link](https://github.com/yakworks/gorm-tools/commit/ec6bdd527b4f7e71b04aadfda18b3ef5841ad4d9)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/bf1565ead717c6d79fd4d59175b4b423a532ae82)
- merge master [link](https://github.com/yakworks/gorm-tools/commit/dd72e1e394cd068c0f8940287cea8f17390e159d)

### v7.3.93

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.92...v7.3.93)
- fix SyncJobCrudApi too so data always shows [link](https://github.com/yakworks/gorm-tools/commit/450ce87b35243cad83497da04aea1d70a48e80c6)

### v7.3.92

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.91...v7.3.92)
- hot fix to always show data on jobToMapGroovy [link](https://github.com/yakworks/gorm-tools/commit/70eae57d3b785397f46e579436f924b36337d45f)

### v7.3.91

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.89...v7.3.91)
- release vGroovyCommons=3.18 [link](https://github.com/yakworks/gorm-tools/commit/402c0a895b88200f465c3c2184b11f7a1b13f759)
- change test that restricted on bad date from today 9/16/2025 [link](https://github.com/yakworks/gorm-tools/commit/32c8a94537aac8dfa9b33cd91b0c9c15b2ddf281)
- release [link](https://github.com/yakworks/gorm-tools/commit/565e2feb2f6199b437a8fb61cf277d1f83e96bbc)
- Merge pull request #927 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/01daa2553e6bafcc5465a5abe9a62662f4d0e29f)
- Bulk import args (#939) [link](https://github.com/yakworks/gorm-tools/commit/c3cdaac14b10b7f5f61ce8580d855de0cb246556)
- fix lint [link](https://github.com/yakworks/gorm-tools/commit/e6b4f7bc6dee7c6a56be18bc9b334c61ee2b099a)
- make payloadFormat consistent [link](https://github.com/yakworks/gorm-tools/commit/dcc6ba6d2ae82bbf951e8f9aa7d6a81b277e3415)
- Syncjob map fix (#937) [link](https://github.com/yakworks/gorm-tools/commit/779e58b52b5935401fc5590c0ebbbb13400c2f2d)
- Add Opaque token support (#936) [link](https://github.com/yakworks/gorm-tools/commit/b3edc069a7e40420329592be5a2de652a053470a)
- doing syncjob so data does not show by default (#931) [link](https://github.com/yakworks/gorm-tools/commit/c536f60472ccd638195cf7624e3832348427b444)
- Add sanity check test for Bulk export end point (#930) [link](https://github.com/yakworks/gorm-tools/commit/849e5674be1fcb6442ca4f1a6e5583ae4d2a0bda)
- add passwrod fields [link](https://github.com/yakworks/gorm-tools/commit/4f3cc07b2c57f65bc21fdffb3544a53e7a177501)
- remove ViolationConverter, the AsMap from commons picks it up, also add fallbackMessages instead of message.propteries (#929) [link](https://github.com/yakworks/gorm-tools/commit/0744fbd9ddadec58cb7f751db64aefdf1c9aa330)
- Merge branch 'dev' of https://github.com/yakworks/gorm-tools into dev [link](https://github.com/yakworks/gorm-tools/commit/5ffa5f3a9e961f1c77ccb33b7d5e4165dbc58d24)
- add projection count tests [link](https://github.com/yakworks/gorm-tools/commit/393d29a0f28e46d444a6798e6a2fd6f2337faaa5)
- try using bean instead (#928) [link](https://github.com/yakworks/gorm-tools/commit/799bffcebfcca354491e25f108253329144af8d0)
- Password error and Violation rendering.  (#925) [link](https://github.com/yakworks/gorm-tools/commit/d551c556a058719322b91d473d6d4e8932fd82cc)
- add link [link](https://github.com/yakworks/gorm-tools/commit/1737a6e550127438a4cf26e4617a11ac3f0d7af2)

### v7.3.89

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.87...v7.3.89)
- update for new sonatype https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuring-your-plugin [link](https://github.com/yakworks/gorm-tools/commit/c5248e4d19babe3e799a3f718291a64689e5398e)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/706c32b75dd727edf66a57306627cbdd3e62a07d)
- Merge pull request #923 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/9928f44184569c76cc29aa4014ed379615a5d6a5)
- SyncJob submit refactor (#889) [link](https://github.com/yakworks/gorm-tools/commit/c0f24ccf35157cfbd74ff6d86cbf1afb56309cb9)
- adds the getNotNull method to repo and domain [link](https://github.com/yakworks/gorm-tools/commit/cd623f9035daaefb8a1c8a6bddd4c132b18c2121)
- Security tripx (#919) [link](https://github.com/yakworks/gorm-tools/commit/2bd092c2dc074c9c53dbf6972597457b732ba398)
- add PasswordConfig and PasswordValidator to SecurityTest Trait for unit tests (#920) [link](https://github.com/yakworks/gorm-tools/commit/20426d9866ac786ec26959e7db906ab466cece77)
- add AppUserPasswordValidator Bean [link](https://github.com/yakworks/gorm-tools/commit/707088c4f5f5fb399eb3d1c5a9e89f204443bd8e)
- Password validation & history (#914) [link](https://github.com/yakworks/gorm-tools/commit/76ae9f8f859f8fc4f8e080d730f6bd100902c7f3)
- Disable create/update/upsert/bulk ops on Syncjob endpoint (#912) [link](https://github.com/yakworks/gorm-tools/commit/bed087665938defc850abcb969ab8a1d0ff918c9)
- Handle query exception (#911) [link](https://github.com/yakworks/gorm-tools/commit/a1ba228a14a94ff0d67dd3d4fc86f06aaed47d3e)
- add get with variable to do check (#910) [link](https://github.com/yakworks/gorm-tools/commit/ff4d127762a05ec822c534b5d3fed4675bfcd090)

### v7.3.87

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.86...v7.3.87)
- release [link](https://github.com/yakworks/gorm-tools/commit/38d9b2d7f0b44678a40c70dc2422ce69ad32f5e1)
- Merge pull request #909 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/fda65ad79e09c5d4d55f0538089e3529b5b698aa)
- Xsl export Fix, support id,name 0r name,label (#908) [link](https://github.com/yakworks/gorm-tools/commit/c5f76bcf023be9e5690c272e8d5f887b047b76fa)
- remove DefaultSecurityConfiguration.addJsonAuthenticationFilter (#907) [link](https://github.com/yakworks/gorm-tools/commit/6a47ce43a8f29e79a732591f6ee80f6bd795c3f5)
- fixes https://github.com/9ci/domain9/issues/3191 (#906) [link](https://github.com/yakworks/gorm-tools/commit/3e6d5241a5c6460bf70dd2d3c327d11b1547f3b4)
- org update : Do not create empty locations (#905) [link](https://github.com/yakworks/gorm-tools/commit/aa4fb3b6a84e118cf453cf5e54bb4fd7152d7e6f)
- Fix : Update org's primary location : Dont create new (#904) [link](https://github.com/yakworks/gorm-tools/commit/5cc5d161dc004e854a7f58e866486f85eede6fdc)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/9d9798adc1e8994e4efb5fb1b0d5ddcdbcf37b6d)
- merge master [link](https://github.com/yakworks/gorm-tools/commit/5a5e50fd5f9f5e9e94138141ad7c4df801a8cf6b)
- merge master [link](https://github.com/yakworks/gorm-tools/commit/f5a4a1e974dbfae515739c9651bcf7340405fb68)
- vGroovyCommons=3.17 [link](https://github.com/yakworks/gorm-tools/commit/1f2bce5bc4dea2e63b6c393745ceb87da16e2b1c)
- disable SecureCrudApi for now [link](https://github.com/yakworks/gorm-tools/commit/e74cf163f009a9c69821677df5e9c93232d9a711)
- diable secureCrudApi for now (#891) [link](https://github.com/yakworks/gorm-tools/commit/71747e1a00a33bda4630669047af8cab271149e0)
- Bulk export (#877) [link](https://github.com/yakworks/gorm-tools/commit/06ce6d3d529c431bdd10bae12cad14c71fa0c6d8)
-  Read only users - Secure CrudApi (#873) [link](https://github.com/yakworks/gorm-tools/commit/45811cf970d1f2d95ac00a4e36e9e6785f7e8e98)
- move AppTimeZone to yakworks commons (#888) [link](https://github.com/yakworks/gorm-tools/commit/0a46d2fd5edc0e93d596dd9cfd0e5be2275e0c6c)
- Support name or id in gridOpts.colModel for excel export (#870) [link](https://github.com/yakworks/gorm-tools/commit/de2f1b3ac1ef58f2f40ecd08e319e80ca72ad273)

### v7.3.86

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.85...v7.3.86)
- trigger release Merge branch 'master' of https://github.com/yakworks/gorm-toolx [link](https://github.com/yakworks/gorm-tools/commit/4853c0bdc999022c3483410a2e8f635f6c0a4f61)
- trigger release [link](https://github.com/yakworks/gorm-tools/commit/43b37ea0d8587ba51257db21853363a99c10990e)
- Dev (#890) [link](https://github.com/yakworks/gorm-tools/commit/0a1ce0dd7c46f67ce679443947a154e82484ca0b)

### v7.3.85

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.84...v7.3.85)
- release [link](https://github.com/yakworks/gorm-tools/commit/a971edb0ade321bfb59d9f832e570d142372ddde)
- Handle exception during excel file rendering (#899) [link](https://github.com/yakworks/gorm-tools/commit/a2d765324913a16ea4ba71a1a0f2cb9ef9fa8e7d)

### v7.3.84

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.83...v7.3.84)
- release [link](https://github.com/yakworks/gorm-tools/commit/e990dc32f71fb394e48cd45fd870acd75809f856)
- Log error before handleUnexpected (#897) [link](https://github.com/yakworks/gorm-tools/commit/ebb2f846abeee9fe5d8727c38efd3c24147328af)
- Mailgun API 500 Retry (#896) [link](https://github.com/yakworks/gorm-tools/commit/78c8283e4f1ae2dad4057e6076d3cb805d1675e7)

### v7.3.83

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.82...v7.3.83)
- release trigger [link](https://github.com/yakworks/gorm-tools/commit/380b0f0da8b25a6a778484bde886a3aad7265a1c)
- dev merge for release [link](https://github.com/yakworks/gorm-tools/commit/18677b210dc2be2c151ac2bf3b738b92f68b1c32)
- Fix NPE - MetaGormEntityBuilder (#883) [link](https://github.com/yakworks/gorm-tools/commit/8de11983b058c2f4fc1a6ae931110d13df148489)
- Get by org (#884) [link](https://github.com/yakworks/gorm-tools/commit/3a0db24baa95269dc613e9fc72ddff54b8c886e6)
- change to bullseye [link](https://github.com/yakworks/gorm-tools/commit/ba3df0d0cdcfccc2d11630b8d2aa7f266659ff51)
- Activity create : NPE fix (#879) [link](https://github.com/yakworks/gorm-tools/commit/13d2b65f6f9ef8c903cb05a83fef42421b829516)
- Api docs (#878) [link](https://github.com/yakworks/gorm-tools/commit/de3dcd270ccdd081ac208a545e3d20737f936a6a)
- MailMessage.msgResponse increase maxSize constrait (#876) [link](https://github.com/yakworks/gorm-tools/commit/be240818685d6430a9559d7be761f20c814b11f1)

### v7.3.82

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.81...v7.3.82)
- release [link](https://github.com/yakworks/gorm-tools/commit/ec89818dbc1c4859d595aadb59af225e5553fe1a)
- bump vGroovyCommons=3.16-SNAPSHOT (#875) [link](https://github.com/yakworks/gorm-tools/commit/e795a35cb218bb9a26ed78d93b9b071f8cc67bd7)

### v7.3.81

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.80...v7.3.81)
- release 7.3.81 [link](https://github.com/yakworks/gorm-tools/commit/0980605b7498ee443a1cf572f10f817f2317dfc9)
- Merge pull request #874 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/d4dc89acd43a3dbd8c091a85eadba2439c5a2a8f)
- vGroovyCommons=3.15 [link](https://github.com/yakworks/gorm-tools/commit/3e035294a52848a2341b6789bad71d15053ccccb)
- MetaMap serialize (#871) [link](https://github.com/yakworks/gorm-tools/commit/b5f7c9eda462f71c40f45cf47b9e4fcb9d736f9c)

### v7.3.80

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.79...v7.3.80)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/3249c60cd1d56326451e476cd10ec84d58b8923a)
- Merge pull request #872 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/4584a122c6de38acd439e1ed48d5a5724a1fb3d5)
- fix null pointer. https://github.com/9ci/domain9/issues/2967 (#869) [link](https://github.com/yakworks/gorm-tools/commit/bb4ff1597f0e6998bf3d03af1e57f34d13cf49da)
- Revert "remove company change for setupMember" (#868) [link](https://github.com/yakworks/gorm-tools/commit/1e0b2ff2d7d227faaf130c2b30231eacffc00b3f)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/541d512c9054f1db3f5dd7a9840081667eb4f4d9)
- Merge pull request #866 from yakworks/revert_orgService-changes [link](https://github.com/yakworks/gorm-tools/commit/9cc080fcc7e3bc06dd166345db983803a87ff3a7)
- remove company change for setupMember [link](https://github.com/yakworks/gorm-tools/commit/b82173aca43bc8b376934efd7f86bf70cef670a1)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/01c1dba0913bfae83ffbf0e731da406dcc3eabba)
- Mailgun logs cleanup (#864) [link](https://github.com/yakworks/gorm-tools/commit/2599f8378571a22b50372c9e15e623133c4e218a)

### v7.3.79

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.78...v7.3.79)
- release trigger [link](https://github.com/yakworks/gorm-tools/commit/7e5973b62d0e08ca357622f2eb7014f17bf5fea2)
- Dev (#867) [link](https://github.com/yakworks/gorm-tools/commit/1b64e25705ca8caff7f894b04f114338e756197f)

### v7.3.78

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.78.1...v7.3.78)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/1a1b1c6ab59e8889b237b87bb964b7f4786c38f3)

### v7.3.78.1

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.77...v7.3.78.1)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/12acb13a3c54c9c1c3b7afbe1e3437b0ffc504d7)
- Merge pull request #863 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/bd8c4054a9c81d5d552a4b57d7f90b5dab3c4624)
- OrgMember : setup company dimension : Fix (#862) [link](https://github.com/yakworks/gorm-tools/commit/53a81c7f28c15c7ab8ffbc12e0b526d5be7cd62e)
- 9ci/domain9#2846 Mailgun : Log error (#860) [link](https://github.com/yakworks/gorm-tools/commit/7993ce743e90d3d37374b0c9b20f7d1de99e25d5)
- subtle null check for unit testing (#861) [link](https://github.com/yakworks/gorm-tools/commit/18765c22cc037a29d0dd13c7e352ed9a2450d045)
- Partition org domain (#853) [link](https://github.com/yakworks/gorm-tools/commit/31648aa4533393f22cf663930cfd9ac99ebd4ce3)
- xls export fix & tests (#859) [link](https://github.com/yakworks/gorm-tools/commit/2ff2211348e7ed4ff306b42af5b991709f94971e)
- Syncjog GET : Add dates (#855) [link](https://github.com/yakworks/gorm-tools/commit/35aa43280d72ae69a02d5153cbdd89d96302e01a)
- spotless fix [link](https://github.com/yakworks/gorm-tools/commit/150d2aefe11bae9dda9bcd4771f823a2cc0d794b)
- java docs for MsgState [link](https://github.com/yakworks/gorm-tools/commit/e6e454758c341c5e436fd3fcf5bedab9a81d5698)
- Merge branch 'clean-up-msgstate-comments' into dev [link](https://github.com/yakworks/gorm-tools/commit/66009004fa199d6e7ced5c19592fc0336d6b7627)
- clean msgstate comments [link](https://github.com/yakworks/gorm-tools/commit/2bc1d18dbda8423027d2722997adae2abb9d4dbd)
- Add test for criteria clone (#854) [link](https://github.com/yakworks/gorm-tools/commit/ab60730e0d98fb51c51c65d3a75f90857aba4527)
- Mailgun config (#856) [link](https://github.com/yakworks/gorm-tools/commit/22f11940939fbe81f8cfb17ee1c3490e1060433c)
- set back to version=7.3.78 [link](https://github.com/yakworks/gorm-tools/commit/91f63de3638636d80f32d144116bf4906129bb09)
- fix mapList visibvility [link](https://github.com/yakworks/gorm-tools/commit/bce695af764e24dd7b500647206ba4d10ca3609b)
- Fn ilike (#848) [link](https://github.com/yakworks/gorm-tools/commit/d8e9e3d4c4b7b155d6d8cc82ae51daa618a3328d)
- basejump [link](https://github.com/yakworks/gorm-tools/commit/6ac451178438968a2f37a112a2d12e618cff2a39)
- basejump [link](https://github.com/yakworks/gorm-tools/commit/c0966d1d9b91f58f5149a7b46cc60a0e950b3f75)
- basejump [link](https://github.com/yakworks/gorm-tools/commit/d5ee543760f72cf6bc13cb7095eb81db282bc672)
- basejump [link](https://github.com/yakworks/gorm-tools/commit/97ba45ec8ca33965a57d18077885fcc124f9c669)
- remove restify (#847) [link](https://github.com/yakworks/gorm-tools/commit/5fd02bb18de6116b5161dad63eb166eec9c8e1fc)
- Grails 5.3.6 groovy 3.0.22 (#846) [link](https://github.com/yakworks/gorm-tools/commit/b1f289567fdae170bd4f8787098d6da228c3413e)
- Simplify gormrepo (#845) [link](https://github.com/yakworks/gorm-tools/commit/3768b05f6165ffd65820f697b209a51465d4ce10)

### v7.3.77

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.76...v7.3.77)
- release [link](https://github.com/yakworks/gorm-tools/commit/8a6c4fd16cb5a09b7941da75300a6bb16a5d23d4)
- 7.3.77 release Merge pull request #844 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/4683db8f362fab34cd5c134e7e3ec251c075c6c6)
- use getQueryArgsValidator [link](https://github.com/yakworks/gorm-tools/commit/8da1c28bd7df0c7ed8a84eb94ba6ec994a30834c)
- add Qualifier on the queryArgsValidator so can have multiple beans and keep that as default [link](https://github.com/yakworks/gorm-tools/commit/395d2c9c967bf1131f80ed69244deb3243f687a5)
- fix fubar(#842) [link](https://github.com/yakworks/gorm-tools/commit/54765ac54cfc424b3e52b13b7f76ef97bee6af49)
- Spring application, hazelcast cache (#836) [link](https://github.com/yakworks/gorm-tools/commit/2e9c5341fe79f276325d46e4e285436512d9ee4d)
- mango query : Handle cast exceptions when building mango queries (#833) [link](https://github.com/yakworks/gorm-tools/commit/93dbf20484c253ae9e47f721b96ba0ddecb930cb)
- Add exception handler for throwable (#840) [link](https://github.com/yakworks/gorm-tools/commit/8d8a80ecf4bd880b2c8f3e3fc7ad504250eb8585)
- basejump [link](https://github.com/yakworks/gorm-tools/commit/f6c2b4db63fe4fe987accc1c8d70ae32c5d6d63f)
- merge master [link](https://github.com/yakworks/gorm-tools/commit/5c91061e48e7eb7834c5fb4483ef328c967998ed)

### v7.3.76

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.74...v7.3.76)
- release [link](https://github.com/yakworks/gorm-tools/commit/c81500897a6573b9551e8f2a4fd789a15a50de27)
- Merge pull request #837 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/226285812f15b3234f8fa30890653e359f705f42)
- basejump [link](https://github.com/yakworks/gorm-tools/commit/b0798f5fe58b13b16a117dd2608177d776a697cc)
- version 7.3.76 sync [link](https://github.com/yakworks/gorm-tools/commit/7fac4ab51a60856c3ae9b9f1733486fd4c123dbc)
- stop excluding id in mango tiday (#835) [link](https://github.com/yakworks/gorm-tools/commit/8fc504d43e31493356f3a358ddfee33f41cdc398)
- Mango jpql list (#834) [link](https://github.com/yakworks/gorm-tools/commit/ef538d9c394e976d3bf85c39d5a83ff4900d333a)
- Fix static exists on entity (#832) [link](https://github.com/yakworks/gorm-tools/commit/a8222091e6ace6de8de5b5b3edcecc1508ac0968)
- Q search validation (#830) [link](https://github.com/yakworks/gorm-tools/commit/21e80d9c6ce0508f5f1543d851ecfb741f781964)
- Mango alias (#829) [link](https://github.com/yakworks/gorm-tools/commit/f8c0be2cd447a52199762f5c6fabb09abe7df8fa)
- Support exportMax for xlsx export (#828) [link](https://github.com/yakworks/gorm-tools/commit/5b117f7a0eeed32fae44b51c681f92dc6f35270c)
- remove ConditionalOnMissingBean from JdbcIdGenerator [link](https://github.com/yakworks/gorm-tools/commit/73a6be33ccfee18e93b29fdd7064a0860e5f3cd1)
- App config (#826) [link](https://github.com/yakworks/gorm-tools/commit/a156a5b3a27be6e081a2b8a8cb584a21203795bc)
- Jpql testing cleanup and QueryArgs has tighter restrictions on how to use it (#824) [link](https://github.com/yakworks/gorm-tools/commit/d4c2fca4482897f6f8e332aacdd329c45150ce64)
- basejump [link](https://github.com/yakworks/gorm-tools/commit/4ff7e79fd9ca359d276e2ac428610802b0a14138)
- jpql and exists (#823) [link](https://github.com/yakworks/gorm-tools/commit/c3ea2def2bcd067ae6ff990b66899fa1cfa91459)
- add where clause to make it easier to parse out (#822) [link](https://github.com/yakworks/gorm-tools/commit/41f9e1c3b68bacc88a6a545cc24b809800f946b9)
- Upsert functionality (#821) [link](https://github.com/yakworks/gorm-tools/commit/3ad089f662c0b12447e2fee9aef24999dca188f9)
- Crud api repo fixes (#820) [link](https://github.com/yakworks/gorm-tools/commit/b3add351e882d97648d61ec2f65ae82e69f3c0a3)
- CrudApiRepo (#818) [link](https://github.com/yakworks/gorm-tools/commit/53de92d3e932744229cc5816105f0f1d17147e34)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/441987e7e7a38ca2262c607951c193228ceee885)
- 1791 query timeout  (#793) [link](https://github.com/yakworks/gorm-tools/commit/d8dbdb32389561e96b8e36b185abb74a157156ef)
- Mail validation (#813) [link](https://github.com/yakworks/gorm-tools/commit/26f0bf428f7ea6172c55b46ef1c15db727d0b4c5)

### v7.3.74

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.73...v7.3.74)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/b2d44519ac186e84f019c419bff534c9910deaf1)
- Merge pull request #810 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/dca49067d030fbb986ad38cbb07895608444a7b9)
- Csv Fix : convert "null" string to null (#809) [link](https://github.com/yakworks/gorm-tools/commit/56612783f2c27e478e4b407051762ce468f5554b)

### v7.3.73

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.72...v7.3.73)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/163b9003fbd89906b661e1f920bbd8909eaa00fe)
- Merge pull request #805 from yakworks/mailgun-logs [link](https://github.com/yakworks/gorm-tools/commit/0bbea97e7728b41e5f437ba176c0ff4e01fbe8ea)
- add log.error [link](https://github.com/yakworks/gorm-tools/commit/291ce37e59dbc6c9810342643bfac82efde16400)
-  Mail gun error log [link](https://github.com/yakworks/gorm-tools/commit/594695e73fcdd852f9729ce7e39c6a01499c006f)

### v7.3.72

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.71...v7.3.72)
- release [link](https://github.com/yakworks/gorm-tools/commit/1f37c34f8446d900c64f7c908240f93b28640e2e)
- Merge pull request #801 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/b0d190f582e817edb50b38ada8afc8eeb58d75d3)
-  Code as String ID (#799) [link](https://github.com/yakworks/gorm-tools/commit/60482ebb885b3b85dd30a2a5bdd32a0830f39fc7)
- Merge pull request #798 from yakworks/797-unit-test-autowired-required-false [link](https://github.com/yakworks/gorm-tools/commit/4df69ea5a0fec858076e2fb311f297ac9d002dfc)
- remove contraints from Company [link](https://github.com/yakworks/gorm-tools/commit/e5bfb8240c291f128ecd78e22ef895f87345cf41)
- Sync the core items from ArSeed [link](https://github.com/yakworks/gorm-tools/commit/49488e64d109d2f4e2cd6eb7e0c292230bf01745)
- remove the @nullable [link](https://github.com/yakworks/gorm-tools/commit/d4fbe0639223e66c915557a20423c1c16da59f7a)
- add feature to be able to List the springBeans to create. [link](https://github.com/yakworks/gorm-tools/commit/fbe263905b62fc01a7adf42f7b3ea12ff22b3961)
- fix up tests now that we always use the SpringBeanIdGenerator for unit tests [link](https://github.com/yakworks/gorm-tools/commit/a03c7109d8de1518bf5d9e8a86fa41b65c4650ac)
- Merge branch 'dev' into 797-unit-test-autowired-required-false [link](https://github.com/yakworks/gorm-tools/commit/582c5cf6e3a07f40ed1ac1e0fee24240dcc0dbbb)
- Autowired default to required = false for unit tests only works for GormHibernateTests right now https://github.com/yakworks/gorm-tools/issues/797 [link](https://github.com/yakworks/gorm-tools/commit/ed68a34a5c242a81b4a662065c223f8142cc3ec3)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/735fe4354841afab99e540e8ebd29eaebec1ac18)
- oapi comments [link](https://github.com/yakworks/gorm-tools/commit/f242bf459fdc9392a8967f8012b55c8cae58df2a)

### v7.3.71

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.70...v7.3.71)
- release [link](https://github.com/yakworks/gorm-tools/commit/0b45bb81b654538ec2bb8728b348b977fc11b83a)
- Merge pull request #795 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/da3ad04120ed5d09449200edf6439455a72ce32c)
- Tests for blankout values (#794) [link](https://github.com/yakworks/gorm-tools/commit/abf7668e49e1ddf9427521e4c25e8235da918056)
-  Default query timeout (#790) [link](https://github.com/yakworks/gorm-tools/commit/0b04845978586a01b118ee6614809b9fbfcd7d85)
- InvalidApiCallEvent cleanup (#789) [link](https://github.com/yakworks/gorm-tools/commit/acbd73258e9341cf83f4a678e4208c4cd9a5681d)
- Api results renderer title fix (#788) [link](https://github.com/yakworks/gorm-tools/commit/22a334029ecec6c59c751864a0bc4e2d0d3aee9e)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/dfdb469da6db5a5ac123c464049918c4632b45b4)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/511e0e8464c2e723d65e7b062a590d1f1276028e)
- Do not log stacktraces for invalid json payloads  (#771) [link](https://github.com/yakworks/gorm-tools/commit/d127d29f9dd44582328d6791f3781bb1b134bec9)
- Phase 1 : Handle assertion errors in controllers (#781) [link](https://github.com/yakworks/gorm-tools/commit/93499a01d0bc8befa1c263c3fbe1cc3358b13aba)
- Api results renderer (#784) [link](https://github.com/yakworks/gorm-tools/commit/323cf91802085d954ac9636d11621d849dea22e1)
- use grid config and uiList for export to excel (#785) [link](https://github.com/yakworks/gorm-tools/commit/5bbf1eb2cdf5569eb7cf2ce22b84f20313416a3f)
- remove logging that was in place for the SyncJob stuff [link](https://github.com/yakworks/gorm-tools/commit/8b147f5fb307cf57c3c0f14ec2695048cc9c7a35)
-  Mango query : Throw error for invalid/missing prop (#783) [link](https://github.com/yakworks/gorm-tools/commit/d34e254890af9c945d02d7ae672ac1d205cd8f89)

### v7.3.70

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.69...v7.3.70)
- use check for syncJob (#787) [link](https://github.com/yakworks/gorm-tools/commit/f5ec384f9146112adcd8ff6d22769214ca374f6a)

### v7.3.69

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.68...v7.3.69)
- Main util fix (#786) [link](https://github.com/yakworks/gorm-tools/commit/bcbf5b89201f3a065854e991a7b4bda781600049)

### v7.3.68

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.67...v7.3.68)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/46d21bb973ee143f4345d8e47cc9f36d743357e0)
- Merge pull request #782 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/77ec7e895a3c9733552dd603cf91638f38f8e131)
- Support html emails for paymailer (#779) [link](https://github.com/yakworks/gorm-tools/commit/76c86df558edf363d31f6aba259791dd7f89909b)

### v7.3.67

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.66...v7.3.67)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/2ed59ef0faf53d26718bacb29acac261c2ff7f54)
- Merge pull request #777 from yakworks/syncjob-hotfix [link](https://github.com/yakworks/gorm-tools/commit/d5a25951ff6eb43facad321c8884f95a92651b48)
- Log Syncjob with empty data [link](https://github.com/yakworks/gorm-tools/commit/e49fd4854697b70d249e0dd65eb7a41669cbf39b)

### v7.3.66

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.65...v7.3.66)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/cce28b88c5a00860cffc23ae4049037756895b10)
- Merge pull request #776 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/b9340955e8de5c4b48f0920469848b043e7d6f34)
- Contact controller tests 9ci/domain9#550 (#773) [link](https://github.com/yakworks/gorm-tools/commit/a054853b6b6cac28dbdee8d4e26006e6a4ce5841)
- Fix syncjob data update issue (#775) [link](https://github.com/yakworks/gorm-tools/commit/2736eef03c39fe965d1e55d02a73bb8c2236d93c)

### v7.3.65

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.64...v7.3.65)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/239ff53e551ef651616883289f507fc79b5f726d)
- Merge pull request #774 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/b976cfe0460890c6825c8e50240f04499a22cb0b)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/8f076a7ff948a6898c511a00217514f0ad8e6b08)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/fe362116afe0640df3dbe3a5641fc81bf03a7ce1)
- Fix default sort by id for projections (#772) [link](https://github.com/yakworks/gorm-tools/commit/48b41889708a7e2b86e8e0a51ae4c72348e32b7b)
- ignore test [link](https://github.com/yakworks/gorm-tools/commit/d645d9f95e8e1bc5631f15ac8e1614bb96c15b41)
- hot fix for default sort id [link](https://github.com/yakworks/gorm-tools/commit/930ca9d423cb84b0134d155b28b0cf25a45b478c)
- Merge pull request #766 from yakworks/list-default-sort [link](https://github.com/yakworks/gorm-tools/commit/52374fad7d113a6f8624f77d447a254957bb6162)
- cleanup [link](https://github.com/yakworks/gorm-tools/commit/d5d32a6b42a5bd4cf54e280fa0e096e7b0cc755e)
- remove pager from method args [link](https://github.com/yakworks/gorm-tools/commit/2696d6bff4cc5310267951e347de6a64c906cf1e)
- Merge branch 'dev' into list-default-sort [link](https://github.com/yakworks/gorm-tools/commit/d90c75b950a5709899c25f12dd2702ae1f4706b4)
- Merge pull request #769 from yakworks/cust-tags-search [link](https://github.com/yakworks/gorm-tools/commit/1d2b725307ecf751bde53c5ac4e7ce3a0854b25e)
- remove tagIds support [link](https://github.com/yakworks/gorm-tools/commit/05ed4a231d09acbcb9746fde645ea3e5b3b3236e)
- comments / add tests [link](https://github.com/yakworks/gorm-tools/commit/cde7f2c7c72f92beceee822e0a33f00a0d4fbb33)
- Merge branch 'dev' into list-default-sort [link](https://github.com/yakworks/gorm-tools/commit/46736cbb1ff9ad96dbdd1a125d2fee8dec387bcb)
- small fix [link](https://github.com/yakworks/gorm-tools/commit/8115311a55b7d5f0a32135782bd6bb43151d4697)
- Support searching customer / account by tags [link](https://github.com/yakworks/gorm-tools/commit/350af3827e6ebd77b81ce573eb1bae7b32a18c4b)
- Merge branch 'dev' into list-default-sort [link](https://github.com/yakworks/gorm-tools/commit/9f8a30612890aabbf0357a88b343dc74321c970e)
- Merge branch 'dev' into list-default-sort [link](https://github.com/yakworks/gorm-tools/commit/449824c2dcdb67b7fb20d151a2ac79c4d0fb284e)
- Merge branch 'dev' into list-default-sort [link](https://github.com/yakworks/gorm-tools/commit/6f279f86e778b937b573992f1227a0a22903c14f)
- Add test [link](https://github.com/yakworks/gorm-tools/commit/285e928c05b109d4b8f350aaca1f1f6b80a4b114)
- default sort by id [link](https://github.com/yakworks/gorm-tools/commit/fb45b1416268fb1d134537560c9510ff65588e11)

### v7.3.64

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.63...v7.3.64)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/ff08f37e738a021371eb8ddf6999a006d63275d6)

### v7.3.63

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.62...v7.3.63)
- Update version.properties [link](https://github.com/yakworks/gorm-tools/commit/bd28f356ec85dbfc433d51ec45f92144a29487af)

### v7.3.62

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.61...v7.3.62)
- release 7.3.62 [link](https://github.com/yakworks/gorm-tools/commit/ff1933b7a660c2df5ca357dc1261cc71b630ab3b)
- Merge pull request #768 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/a03c6a87b4533e18d420f092f30cc9236730d372)
- get org from q criteria (#765) [link](https://github.com/yakworks/gorm-tools/commit/945a2bb22469549f096978ae673c772b6f0de821)
- Repo exception handling (#749) [link](https://github.com/yakworks/gorm-tools/commit/403e6402c48bcc043a3759bede702e414f590cf2)
- Support querying by uuid string (#767) [link](https://github.com/yakworks/gorm-tools/commit/2a5ae8694ab31fa534716502a436246dd0e1e592)
- #2162 Add InvalidApiCallEvent (#763) [link](https://github.com/yakworks/gorm-tools/commit/029be08cc172f21985bad8655dcdc866a2bfffe6)
- 9ci/domain9#2440 , 9ci/cust-rndc-ext#325 (#764) [link](https://github.com/yakworks/gorm-tools/commit/746922701dad691657632f7734922396d72a4d19)
-  Fix tidy map to use id for inquery if key exist in map list (#762) [link](https://github.com/yakworks/gorm-tools/commit/2774f07214b015ec37899c2e2a2d72a6b107f0e2)
- Stack helper (#760) [link](https://github.com/yakworks/gorm-tools/commit/9d165043c25044271394aae4528bb69df3466be7)

### v7.3.61

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.60...v7.3.61)
- Merge branch 'master' of https://github.com/yakworks/gorm-tools [link](https://github.com/yakworks/gorm-tools/commit/170d44d184aee73d6618ab42fe698cca5ada423c)
- release [link](https://github.com/yakworks/gorm-tools/commit/9425056a0b19190532a0dec320be9928718956d0)
- dev merge for 7.3.61 release [link](https://github.com/yakworks/gorm-tools/commit/25f4d49e4134d4ab68003d9c851887fc35716619)
- ignoreUnknownFields=false on OrgProps [link](https://github.com/yakworks/gorm-tools/commit/ae91bcb2243fa9b7db4236a278e9ebc9f355009b)
- cronJobProps (#757) [link](https://github.com/yakworks/gorm-tools/commit/5b251a0999a872129fa3ebf02e804926404d469a)
- App time zone (#756) [link](https://github.com/yakworks/gorm-tools/commit/8fa86aaf68ebeab142402a5e47792d4c312f748b)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/9682fec5401b6b4545934fc7c3f3a59c2c88f00b)
- bump version to 61 [link](https://github.com/yakworks/gorm-tools/commit/74d64a8a49b3206f155f15e36ee2fa204ec407bc)
- merge master [link](https://github.com/yakworks/gorm-tools/commit/9f0135d612061466cf97844085be9e74dd98059e)
- Merge pull request #752 from yakworks/release/dev-next [link](https://github.com/yakworks/gorm-tools/commit/6c4b8b7903da88c2e32a28840bd7e1ac5288329e)
- add getter for partitionOrg members to centralize common logic in OrgService [link](https://github.com/yakworks/gorm-tools/commit/10e023dfca327d4af93ec1af0f10b586ae8a927a)
- rename OrgMemberService to OrgService to prep for it doing more. [link](https://github.com/yakworks/gorm-tools/commit/ba0e88c063d8bf25bfde6be40b8c6dc7132a2529)
- Cleanup (#750) [link](https://github.com/yakworks/gorm-tools/commit/9e576f9a76d291c9751b2f6abd2103f1d26f7907)
- setup OrgConfig and refactor OrgDimensions (#737) [link](https://github.com/yakworks/gorm-tools/commit/2367bba6796eeaad37cbff9a9539233197471ae8)

### v7.3.60

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.59...v7.3.60)
- release [link](https://github.com/yakworks/gorm-tools/commit/3f527e38b647e09f4b3781e1dca6098e74785517)
- SyncJobArgs Default async=true (#754) [link](https://github.com/yakworks/gorm-tools/commit/2da5f643bcc5a89c67c22a73fbaae4256a16b06f)

### v7.3.59

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.58...v7.3.59)
- release 7.3.59 [link](https://github.com/yakworks/gorm-tools/commit/6ec5ae870861a33fb02b1be10b5a8fdb66fb2851)
- Merge pull request #753 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/acc59b7a4956b85178859766132c9a5b0f96ff76)
- Catch exception (#751) [link](https://github.com/yakworks/gorm-tools/commit/e14e5bcf8e3c205ecee6bcac06229fe776692937)
- Merge pull request #748 from yakworks/hypersistence-upgrade [link](https://github.com/yakworks/gorm-tools/commit/aaaf1a37da309e6116341b94160e33f42363899d)
- move to io.hypersistence.hypersistence-utils-hibernate-55 3.5.2 [link](https://github.com/yakworks/gorm-tools/commit/36c4ac8d4a4391ed4d3493acd3422d55a76d0bfa)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/514255d5ecc9b7d56788e75fce840a2b6b828be8)
- Merge pull request #736 from yakworks/2127-activity-date [link](https://github.com/yakworks/gorm-tools/commit/0558e8b2f7e3ff49becddb890174d6e339d14abe)
- Merge branch 'dev' into 2127-activity-date [link](https://github.com/yakworks/gorm-tools/commit/5ea3a323058490da615d283faf589758709edfe5)
- update constraint [link](https://github.com/yakworks/gorm-tools/commit/f3ab3f25b05e65498ba540b1c0788cef2545e68f)
- cleanup, add tests [link](https://github.com/yakworks/gorm-tools/commit/d66b04bd0e73e4e7048372bc0ffead30d31055a8)
- 9ci/domain9#2127 Add actDate field to activity [link](https://github.com/yakworks/gorm-tools/commit/6eb1febd0a075316c4abff9f0c84a3adf519cca2)

### v7.3.58

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.57...v7.3.58)
- release [link](https://github.com/yakworks/gorm-tools/commit/f96be053f0f954bd11facc6370c065718fa45aa3)
- Merge dev for release 7.3.58 [link](https://github.com/yakworks/gorm-tools/commit/f1753f8aa0800223e79c1fbc3ca03b23a4aa0b8a)
- vGroovyCommons=3.13 [link](https://github.com/yakworks/gorm-tools/commit/bf57694cec2b76ebac6b01cab52673e8a3004167)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/84e0e0adc7c1aa090fd7070f67625d6541659b13)
- remove the flush and clears from RepoUtils (#744) [link](https://github.com/yakworks/gorm-tools/commit/66cbd970e2d294b0f6c1a235d15c683fb239f822)
- Hibernate session cache and query gotchas (#742) [link](https://github.com/yakworks/gorm-tools/commit/4d896411648b876f8e3f69843b14ea0ea3b95403)
- Merge pull request #743 from yakworks/2292-bindid [link](https://github.com/yakworks/gorm-tools/commit/896c4c56e1be7f07edecb3fbc46ff46f9749857e)
- support bindId during post [link](https://github.com/yakworks/gorm-tools/commit/a7df89714843d47356bebe41017589d570306bf0)

### v7.3.57

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.56...v7.3.57)
- try cache bust [link](https://github.com/yakworks/gorm-tools/commit/cbaa88d8519bb9596597a90084fa4966549d2aae)
- fix so tags gets copied from MailMessage and sent through mailgun (#745) [link](https://github.com/yakworks/gorm-tools/commit/278a45338e4724a720d7b8b332c2653550e73980)

### v7.3.56

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.55...v7.3.56)
- RestResponderService and maint window bloacker for jobs [link](https://github.com/yakworks/gorm-tools/commit/7c5e15ccf9577cb81c68ae8a5ac8c2e3c0924dd4)
- move to RestResponderService, use the orm builder for the json ones w… (#740) [link](https://github.com/yakworks/gorm-tools/commit/db104131be5bde12e0319f1fe4c528bf3c3bd83d)
- add check for jobs to see if its in maint window, also a bunch of lint/codenarc fixes that were bad. (#739) [link](https://github.com/yakworks/gorm-tools/commit/7903a5ef831f751459573b7ac6e0794229c105af)

### v7.3.55

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.54...v7.3.55)
- release 7.3.55 [link](https://github.com/yakworks/gorm-tools/commit/39acc9e86f930538b0074a79172bbc876b506475)
- Merge dev for 7.3.55 release [link](https://github.com/yakworks/gorm-tools/commit/8203573ad09e668fbd664e6472009c920bbc167c)
- vSpringGrailsKit=5.1 & vGroovyCommons=3.12 [link](https://github.com/yakworks/gorm-tools/commit/1ff2cd240fbcbe1a2faf0de3e96de94f11354d22)
- fix catch [link](https://github.com/yakworks/gorm-tools/commit/d9f6f4fde3c585f696bed73733b1ede37dee05b4)
- catch throwable so asserts get caught and it finishes job. (#735) [link](https://github.com/yakworks/gorm-tools/commit/4cbc5954431e754d1a9206146ea8daaeeb02eb51)
- Release/jobs (#734) [link](https://github.com/yakworks/gorm-tools/commit/0967b9c85dcb4bc3911cd282817470b644ce09fa)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/d686a7d1ceceb9bf1e6b55e46d9b5d25086b99d9)
- Wrap exists in trx (#733) [link](https://github.com/yakworks/gorm-tools/commit/db24d82ee0fb98ef5fd338e3603954792af266de)
- Merge pull request #729 from yakworks/release/dev-next [link](https://github.com/yakworks/gorm-tools/commit/98a91932c3ce3f27232e3ed16d84632ec9f2c5a7)
- Merge branch 'dev' into release/dev-next [link](https://github.com/yakworks/gorm-tools/commit/f1ebfb8061d429ee9cb30b28a5b3cf549f6ee6b7)
- bump vSpringGrailsKit=5.0.11-SNAPSHOT [link](https://github.com/yakworks/gorm-tools/commit/d74f65a54a251861aec905bca79c6cb63b1b7ea3)
- Merge branch 'dev' into release/dev-next [link](https://github.com/yakworks/gorm-tools/commit/1a5d06899a28fa0a6a3a74c83bdbacd91fc2126f)
- Jpql exists (#725) [link](https://github.com/yakworks/gorm-tools/commit/197e895d28856a125df10f30d46b9dcab4aa8138)
- JPQL enhance, commons 3.12 (#720) [link](https://github.com/yakworks/gorm-tools/commit/f259fe44f81615eca89a7315088922fa748b8996)

### v7.3.54

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.53...v7.3.54)
- bump vGroovyCommons=3.11.1, and release [link](https://github.com/yakworks/gorm-tools/commit/6dcc7891ffadc25c43a743eaca69296b4429e6f6)
- release [link](https://github.com/yakworks/gorm-tools/commit/9dbe2636fecd95894896d384e7e2354390cc53a3)
- v54 - Merge pull request #732 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/d1ccad4d3de36c8136d2671a877ac5715371a41e)
- #2255 Add check for contact source unique key (#730) [link](https://github.com/yakworks/gorm-tools/commit/0d72c021c147dd65c0d40fd710f8a60cbec0462b)
- Handle broken pipe exception (#731) [link](https://github.com/yakworks/gorm-tools/commit/ef8cb57f2f6187eb3f856db4ae1b81c47f2a090f)
- add SyncJobContext.updateMessage for updating messages without a result. [link](https://github.com/yakworks/gorm-tools/commit/7e602ccac39a40f0f9f31567c35b64f48e529c21)
- commons 3.11.1-snapshot, dep hell on handlbars springboot plugin, got rid of humanize, and got versions in check [link](https://github.com/yakworks/gorm-tools/commit/7d1ec8dc655d3186ab9e1f4f1373a05fd75add3c)
- 9ci/domain9#2222 login should be case insensitive (#726) [link](https://github.com/yakworks/gorm-tools/commit/a1fdbd5bfca44c4035e4345473767af5a6b454f8)
- add clone to MailerTemplate (#728) [link](https://github.com/yakworks/gorm-tools/commit/115e143f56998f535f5ef75efc813af7ad61cd64)
- Common mailer (#727) [link](https://github.com/yakworks/gorm-tools/commit/e0a6811430ff951774d84fa9d0d7686bd3cdd08a)
- Fix to handle db exceptions and convert to problem during remove (#722) [link](https://github.com/yakworks/gorm-tools/commit/3bfbb897f059c4d39705b27739b0ff8a7a946170)
- Fix binder to handle hibernate proxies (#724) [link](https://github.com/yakworks/gorm-tools/commit/cbd0e2ea4700fa57d4040c2341b0b9dfefb2c388)
- unwrap spring proxy before looking up event methods on repo (#719) [link](https://github.com/yakworks/gorm-tools/commit/821b6caf208ac1e5a20e0d75cda5f386b5bb408a)
- rename getCriteria to buildCriteria() (#717) [link](https://github.com/yakworks/gorm-tools/commit/e5a4afd36d2515c40801907815d926db2dcc34ce)

### v7.3.53

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.52...v7.3.53)
- Merge pull request #716 from yakworks/thread-safe-ComboKeyExistsQuery [link](https://github.com/yakworks/gorm-tools/commit/c14ccfecf901dca0fdcf1e3e226987cb922eacdd)
- prep for release [link](https://github.com/yakworks/gorm-tools/commit/a2f20b6fb98d776f8dde388f2c649b96324443fc)
- make ComboKeyExistsQuery, build the string each call, will be fast and no need to cache it. [link](https://github.com/yakworks/gorm-tools/commit/9faee6bef1e6fe147d25f36393521d55f12d1907)

### v7.3.52

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.51...v7.3.52)
- release [link](https://github.com/yakworks/gorm-tools/commit/7b137a6c61d8999157fcba6a62a69c21424f61bd)
- Merge pull request #715 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/8b9e214d670fb1b3a798e3e52f1e5437b163ad90)
- test for hasChanged and isDirty (#696) [link](https://github.com/yakworks/gorm-tools/commit/f5680d6ab788beac3ea51151c8fd7572ca68ccb1)
- Allow csv for bulk instead of only zip (#711) [link](https://github.com/yakworks/gorm-tools/commit/b0b399cb7ad84089e33267eb0a6b402a1ade836b)
- Misc refactors (#710) [link](https://github.com/yakworks/gorm-tools/commit/1fe0ef6916718971f4ddefc2eb63de619d19cd1a)
- 9ci/cust-rndc-ext#174 set contact as primary contact - if isPrimary=true (#709) [link](https://github.com/yakworks/gorm-tools/commit/ecd121099e50bd987f8b4c9ff224a9b249f20368)
- Data binder fix  (#708) [link](https://github.com/yakworks/gorm-tools/commit/cbe6b520a2b1a5081a0a352140df53fb3fcac70b)
- Api generator fix : handle space without any entities (#707) [link](https://github.com/yakworks/gorm-tools/commit/bfccba7479f3f23f2c154cb42d3089a0dd8d2f15)
- uuid fix (#705) [link](https://github.com/yakworks/gorm-tools/commit/4da93f25847b46ce9dcb2851932f57c1b28b3064)
- 9ci/domain9#2085 Add helper to setup syncjob args and make consitent.  (#700) [link](https://github.com/yakworks/gorm-tools/commit/ab7860d3c6e6cc206d27a55c8f7b7ce2eb6c5f1a)

### v7.3.51

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.50...v7.3.51)
- release [link](https://github.com/yakworks/gorm-tools/commit/8c4d07a082f189e164d542c57a4c73bcdf1d4499)
- Merge pull request #694 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/c7823be634fd50f9a56ae2e8ae050927f9db6e7b)
- fix how exists work for mango (#693) [link](https://github.com/yakworks/gorm-tools/commit/e7ffd98dec7eba67d9ebb5d741b2db262fb987ac)
- add tests for KeyExists and ComboKeyExists (#692) [link](https://github.com/yakworks/gorm-tools/commit/22279438f6531d334160663b8312ff46e0eabee6)
- setup default for maxSize of 255 for string, for TEXT columns types jack up to reasonble levels, maxSize: 65535 for note body (#691) [link](https://github.com/yakworks/gorm-tools/commit/46ffbb6b4cdc3f7d6b787afba4a89dfdacd195b6)
-  Add max size constraints (#687) [link](https://github.com/yakworks/gorm-tools/commit/22d5776fea23aee4091825acf1752266d428030a)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/55e75d82080badbe5d1ed2bc4d946711dc989587)
- trigger [link](https://github.com/yakworks/gorm-tools/commit/6db734a9a59442fefc30456264524e952b5a3e91)

### v7.3.50

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.49...v7.3.50)
- ignore flaky test [link](https://github.com/yakworks/gorm-tools/commit/d978deeb99a447c77ed359c85e1e423590402ea5)
- release [link](https://github.com/yakworks/gorm-tools/commit/b23117d89d56991fdaac97c06b1bd988502af6e0)
- Add tests for contact create (#690) [link](https://github.com/yakworks/gorm-tools/commit/b59f27a24db24229666daf9884843ea994712b18)

### v7.3.49

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.48...v7.3.49)
- trigger [link](https://github.com/yakworks/gorm-tools/commit/d5ecc78920a8dc8ae84e594b1e5b5073e10a567b)
- blow error to avoid stack overflow, not a long term solution but work… (#689) [link](https://github.com/yakworks/gorm-tools/commit/5c0a98416c1268c5af197ba1db6237a052ae943f)

### v7.3.48

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.46...v7.3.48)
- release [link](https://github.com/yakworks/gorm-tools/commit/3cd8fc798d456eedbb352a39df017e265ab5a7ea)
- Dev (#686) [link](https://github.com/yakworks/gorm-tools/commit/b0750efd7704a816c32c71317c904cb3a41c0246)

### v7.3.46

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.45...v7.3.46)
- downgrade to warning for lost params so we can turn it off easier [link](https://github.com/yakworks/gorm-tools/commit/a2c50f987bd1d693837e48fe5940e888eee5c5e6)

### v7.3.45

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.44...v7.3.45)
- another iteration on logging for the lost params [link](https://github.com/yakworks/gorm-tools/commit/23618649104d9adde6a938181871fbe9ba7f3608)

### v7.3.44

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.43...v7.3.44)
- release with logging for parsedParams [link](https://github.com/yakworks/gorm-tools/commit/5042dc8448bb65db961478cf3ef0f4a5d182795f)

### v7.3.43

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.42...v7.3.43)
- release [link](https://github.com/yakworks/gorm-tools/commit/dc66867a8676ccb3ff7b4667ab1c359868dc1fab)
- Logging hotfix 2 (#676) [link](https://github.com/yakworks/gorm-tools/commit/eb9794b95a050760acc04e429df84d7b176c65d2)
- add logger and logging for lost params that have been reparsed (#675) [link](https://github.com/yakworks/gorm-tools/commit/004214a95c29cd0eda20f797b8c181b760498a3b)

### v7.3.42

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.40...v7.3.42)
- release [link](https://github.com/yakworks/gorm-tools/commit/c46ced546d70f9f0ebc719fb52d029d8838fea95)
- Merge pull request #674 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/63c323af54d57cf69ddd0bb5bda3e8f599a89854)
- fix null for icu4jArgs when null arguments [link](https://github.com/yakworks/gorm-tools/commit/4fcca857b6eff101562a0d0a5115cc9dd99ba216)
- testing mango and add getMemberOrg for OrgMebers [link](https://github.com/yakworks/gorm-tools/commit/6825c6d012a40f612ddd13955c6aa20ec6984d80)
- Params map (#672) [link](https://github.com/yakworks/gorm-tools/commit/18332f668512b11b7a2081f3347a5b9f91007604)
- 1941 check if orgsource exists (#673) [link](https://github.com/yakworks/gorm-tools/commit/f2df1b50e1c29f0f858baff928a9a41c8c9692a4)
- #1925 Handle and report back bad query in q param (#670) [link](https://github.com/yakworks/gorm-tools/commit/9ba95357fddb4916e51a0a866118abaeda1f7cd1)
- isNewOrDirty ok for any entity, add tests (#671) [link](https://github.com/yakworks/gorm-tools/commit/e70a5a83ee97070d115883048604de55aa343bbe)
- 9ci/domain9#1924 Parse params from query  (#669) [link](https://github.com/yakworks/gorm-tools/commit/366063402eb0fd973eeca43a582aaf188f9bbc73)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/c8dbeb6309e54777f54188c240e5368d3cb4e1aa)
- Add companyid to orgmember (#664) [link](https://github.com/yakworks/gorm-tools/commit/73e95840b7fe8e997189c782ba61dee02f943869)
- Rest api refactor (#667) [link](https://github.com/yakworks/gorm-tools/commit/1444bee4ea9a891cb28925b932bd1443bd126508)
- token comments and docs [link](https://github.com/yakworks/gorm-tools/commit/92e46af756b117576c780ce56d7092f56d3186a3)
- finish params refactor (#663) [link](https://github.com/yakworks/gorm-tools/commit/53250b867b1bd2829a9790d4ceb066c3dfdee06b)

### v7.3.40

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.39...v7.3.40)
- fixes for jpa and nested memebers (#668) [link](https://github.com/yakworks/gorm-tools/commit/060b098d066478e2179753da18aa83d39fed4d34)

### v7.3.39

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.38...v7.3.39)
- deep sanitize stack traces to try and make them more managable [link](https://github.com/yakworks/gorm-tools/commit/83c0c8d98aae2f7cc5e1555cdeb926dfded4dbde)

### v7.3.38

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.37...v7.3.38)
- Oktrait (#662) [link](https://github.com/yakworks/gorm-tools/commit/c88af829d166c23f59aea1c6d5b0ac1f0e487e1a)
- add ablity to change the getLoginTokenPath [link](https://github.com/yakworks/gorm-tools/commit/957cc4d3571e3f31e7670853c3754ef7b1cef3cb)

### v7.3.37

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.36...v7.3.37)
- release [link](https://github.com/yakworks/gorm-tools/commit/82f76ad9a0116f7a4df0e45594be1145bf705e73)
- trigger release [link](https://github.com/yakworks/gorm-tools/commit/a62d937213e712f04a42ba2d2d3a8a71b9f0464b)
- fix op:remove on crossRef items, add test for it. (#660) [link](https://github.com/yakworks/gorm-tools/commit/0dba63c31852dbe395b04ae04d442f17666561cb)
- Fix tags handling domain9#1708 (#658) [link](https://github.com/yakworks/gorm-tools/commit/504bae5c1151ac280dd063cb31234fd882a7ed63)
- Merge pull request #659 from yakworks/mailgun-utils [link](https://github.com/yakworks/gorm-tools/commit/78b2cb6c64a491f15f9f6a919894deb163d1c921)
- mail utils [link](https://github.com/yakworks/gorm-tools/commit/9d6138b9f5fa61301941bf6daa782862ae2f8904)

### v7.3.36

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.34...v7.3.36)
- release [link](https://github.com/yakworks/gorm-tools/commit/228d2fd9d368336272a80b2fbca562306aada185)
- Merge pull request #657 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/333cee5c8ed313b56c4c283cc98b14197cec3aee)
- DataProblemException is bulk data is empty (#656) [link](https://github.com/yakworks/gorm-tools/commit/1db7e66310f804ff4b38a938b6ecc3a39542eb0a)
- fix version [link](https://github.com/yakworks/gorm-tools/commit/6fdd231ebd67b76161b54ea31123c4b1124f421d)
- mail configs (#655) [link](https://github.com/yakworks/gorm-tools/commit/58e802563e0695b5cb750ba57b0ff7610038a44e)
- Grails 5.3.2 and spring-boot 2.7.9 [link](https://github.com/yakworks/gorm-tools/commit/2b7a169e2f180729420e9041da4dfb3337a07d83)
- change the OapiSupport to thread safe instance strategy [link](https://github.com/yakworks/gorm-tools/commit/663c061db88a0316189813eb65fe93f13a8f1764)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/b522f1e1cd5fdd60f37a7deaf0b393617553e6c2)
- prep for release [link](https://github.com/yakworks/gorm-tools/commit/c188e2a68fc37e7f79e3df0c89487c0b0eba2f22)
- update sendDate when mailMessage is sent [link](https://github.com/yakworks/gorm-tools/commit/87f3bdb7eea221b84759f597a1b7b667f293a916)
- fix attachment and test in convertMailMessage [link](https://github.com/yakworks/gorm-tools/commit/503aabba3a3f1bb197552f56a9b258b2ae8c314a)
- mailgun config (#651) [link](https://github.com/yakworks/gorm-tools/commit/fb6533138a8afefc9d28ac3197cff700da4a463d)
- Mango sort fixes (#650) [link](https://github.com/yakworks/gorm-tools/commit/0aa6b1028632214267bf02094118036aa1c53aee)

### v7.3.34

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.33...v7.3.34)
- Release dev (#653) [link](https://github.com/yakworks/gorm-tools/commit/f05f10a721e2a059f67bf1ca63c21a4f5c59fe5f)

### v7.3.33

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.31...v7.3.33)
- qRequire, Jasper, Mailgun, JPAQL Builder fix and tests (#644) [link](https://github.com/yakworks/gorm-tools/commit/3847dbf2b10219d7ee145509e49019fa772def53)

### v7.3.31

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.30...v7.3.31)
- trigger release [link](https://github.com/yakworks/gorm-tools/commit/f33d753c59cf700b69e67590a005aea0abca86e3)
- JpqlQueryBuilder #638 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/5433c92c83fa62345882b2be2b812309e7734f27)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/8def5bfdb54f521293cefe6637b1f084c392f4fa)
- lint fix [link](https://github.com/yakworks/gorm-tools/commit/ea562c8ec803b18ef036a0b10f4e6f3bd1378db0)
- Jpql query builder (#637) [link](https://github.com/yakworks/gorm-tools/commit/e96b8bd6bc21a81d1e0c89ee13e7dd31c346a424)
- Fix jpql  (#636) [link](https://github.com/yakworks/gorm-tools/commit/d88c86d66f6deb5893e5e19f40fee6b7bceab3d7)
- Add queryGet method to repos and entity (#621) [link](https://github.com/yakworks/gorm-tools/commit/dcba6d50b54cac1ffde3724fab1c1993ccc76df5)
- Add a test for restrictions on a projection with alias (#619) [link](https://github.com/yakworks/gorm-tools/commit/377b27a7a6ef01b47ee78f183f12198d04bfbcb7)
- Fix some XXX (#632) [link](https://github.com/yakworks/gorm-tools/commit/08fc7c4975d931eed91968af6cf2112509546c4d)

### v7.3.30

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.29...v7.3.30)
- release [link](https://github.com/yakworks/gorm-tools/commit/ace07cca0e99b39aeefe60f9c4cd41b88fdbb25e)
- Merge pull request #634 from yakworks/dev [link](https://github.com/yakworks/gorm-tools/commit/73ae2d646747fb20a068d58a27ca8c6ecf32c8c8)
- Merge pull request #633 from yakworks/fix_hasRoles [link](https://github.com/yakworks/gorm-tools/commit/9038f27bbcc38c35fb1da4fa3933a0a3f542923c)
- fix log noise in mango and bump timeout for testing in config [link](https://github.com/yakworks/gorm-tools/commit/1ef86090f57628aa5d7cafec778e7a798764e7fb)
- dont use springs hasRoles [link](https://github.com/yakworks/gorm-tools/commit/7916d60ab66494d3b157fffafc46005af77e983a)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/2d84a4e840d697f2fbfcbf4237c370dffe7de114)
- back to release=false [link](https://github.com/yakworks/gorm-tools/commit/932688691c34b5cc6b21f0ec4f9f1c09a7e86da8)

### v7.3.29

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.27...v7.3.29)
- New Security, bump springkit, spring boot  (#581) [link](https://github.com/yakworks/gorm-tools/commit/6b0c41cd27179fb789e253ced0b5b6667dd7bf5a)
- set release true [link](https://github.com/yakworks/gorm-tools/commit/cf77e246cc629087494f3f714c61b1261231a37c)
- SpringGrailsKit=5.0.9 [link](https://github.com/yakworks/gorm-tools/commit/0b5b018e2986cd499ce4fa9c6115d1caa789a524)
- Merge pull request #630 from yakworks/1668-contact-update [link](https://github.com/yakworks/gorm-tools/commit/42e353c8592d926dec3c52db9cb075098f3e854a)
- cleanup [link](https://github.com/yakworks/gorm-tools/commit/94c59d99237979d6a5e9c2cbd3c3d4f74b87fef1)
- 9ci/domain9#1668 Contact update with source id [link](https://github.com/yakworks/gorm-tools/commit/07a6d12034a9a50f9a446238d90e18f5b99771d9)
- Merge pull request #629 from yakworks/contactsource_sourceId_lookup [link](https://github.com/yakworks/gorm-tools/commit/625ebdd56a9d1c070caf3e8ac907016b40e61c39)
- use sourceId lookup contact [link](https://github.com/yakworks/gorm-tools/commit/bc0b50e86ba643c5ee06493f7d6e32de903b11a5)
- security. add log debug to make it easier to diagnose security [link](https://github.com/yakworks/gorm-tools/commit/4623432aa40d62ed4556c063557ce534d662e2ae)
- Okta security (#628) [link](https://github.com/yakworks/gorm-tools/commit/2384c91e503554347cbf7993031ce7522ceffa8d)
- add jwt-exchanger and test (#627) [link](https://github.com/yakworks/gorm-tools/commit/1e9c0a6b59ed6c40a71a57d6dd2068a0e6dfcfb7)
- Violation errors (#625) [link](https://github.com/yakworks/gorm-tools/commit/f0786fd4d4df4ef70a70cef24ff00392e4267100)
- Triggering circle [link](https://github.com/yakworks/gorm-tools/commit/b310dc5aabc9f138e5c3b5e58bd30c09f7e196e0)
- fix problemsToString in SyncJobRenderer [link](https://github.com/yakworks/gorm-tools/commit/714da9466023952b9d1f6bba0bdb65d94d437003)
- 1562 syncjob error field (#623) [link](https://github.com/yakworks/gorm-tools/commit/bb294c9d8d252d1aed251be70af6cf767f71fee6)
- Merge pull request #624 from yakworks/1586-org-calc-amounts-defaults [link](https://github.com/yakworks/gorm-tools/commit/0ab79d773caa9db61e70e80f4f7bf6ab13d6bac8)
- 9ci/domain9#1586 - Orgcalc amounts defaults to zero [link](https://github.com/yakworks/gorm-tools/commit/5e2f4dd12ffcb8c17089af2c9e6654fac53cf81a)
- add runJob helpers in SyncJobService based on runnable instead of supplier (#622) [link](https://github.com/yakworks/gorm-tools/commit/1605c0f29beea3af012598dacb489a181a6512ef)
- transformResults in SyncJobContext has redundant set to data prop [link](https://github.com/yakworks/gorm-tools/commit/9c6b6b1b54ae817c93d6449a814b500cfe7f76e8)
- add common runJob method to make things a bit more dry (#616) [link](https://github.com/yakworks/gorm-tools/commit/5304cc1a08237046fe6a7d813fba79d20f06c7a1)
- allow alias name on projections in mango (#615) [link](https://github.com/yakworks/gorm-tools/commit/3a8dace5de5caf4bbcc7418482cdf6caa816c20a)
- SyncJobArgs.promiseEnabled renamed to SyncJobArgs.async [link](https://github.com/yakworks/gorm-tools/commit/6c3827003fb97e5ea0f556a5d3baa714322c9d3c)
- SyncJobArgs.asyncEnabled -> SyncJobArgs.parallel [link](https://github.com/yakworks/gorm-tools/commit/6940215483f70823fb9f9a6067d9898978a39f4b)
- syncJobArgs.promiseEnabled and syncJobArgs.asyncEnabled with better docs and make consistent [link](https://github.com/yakworks/gorm-tools/commit/f07aba88bcbff7d92e7a21ea70e9189b62cf9b90)
- run open-api docs by namespace group. [link](https://github.com/yakworks/gorm-tools/commit/952f2338d9ac80e5186c17390ea191cc72b2ab0d)
- modify SyncJobEntity so irellevant fields dont show in api docs. sort the paths in api-docs alphabetically. [link](https://github.com/yakworks/gorm-tools/commit/71ee5858d83012f6ae86fcbfa616fb8d381a19b3)
- Sync job with problem (#614) [link](https://github.com/yakworks/gorm-tools/commit/37f106d01dd6d496c7761b453ec520a5e23f8555)
- Api context path (#613) [link](https://github.com/yakworks/gorm-tools/commit/6c169394acdfd337809fe3bf128b9a065f323aee)
- Org member service not required, refactor RallySeed  util (#611) [link](https://github.com/yakworks/gorm-tools/commit/b1942a356253e3e57bb874eeee2d8c499b84ec74)
- #603 Keep original property name in projections, dont prefix _sum etc (#608) [link](https://github.com/yakworks/gorm-tools/commit/1aa9b7d93d3faa5050f0a521f847b7fa1863ad4d)
- use ES256 as default for JWT instead of RSA256 (#606) [link](https://github.com/yakworks/gorm-tools/commit/9cd3a831d487d1da193126810e13738d0bf89309)
- change to expireDate from expireAt for token domain. fix TokenStore so it ConditionalOnMissinBean [link](https://github.com/yakworks/gorm-tools/commit/04284e161fe618c3362fa94a67de337fb94e24c9)
- remove duplicate bearerTokenResolver bean from rcm-api [link](https://github.com/yakworks/gorm-tools/commit/49854f0a44410135e1772d30c75f0a805cf45719)
- token storage, backwards compatible (#598) [link](https://github.com/yakworks/gorm-tools/commit/2014802bafd25f30d315cd249231ef8d9369e339)
- cleanup, make appCtx a dependson so its done early. add grails-kit to loadAfter on grom-tools [link](https://github.com/yakworks/gorm-tools/commit/ebc9de0378582d45fd5d606cf2ece5f2a1c7c147)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/b271c928ef7cfdffaa28ac03ad5ca6e87f555855)
- 9ci/domain9#1420 - support loose json string for tags add/remove (#601) [link](https://github.com/yakworks/gorm-tools/commit/21973d7ebf23dba8390243ce9056b9ea4dcaa6cc)
- Delete location with contact (#599) [link](https://github.com/yakworks/gorm-tools/commit/d35b8266ecb930abcedf3a0bd8cd520e6a54faba)
- #478 Handle XXX / FIXME (#595) [link](https://github.com/yakworks/gorm-tools/commit/c2dbfcae933331623f0bd3838fdb30e152a05d85)
- FIXME updates and cleanup [link](https://github.com/yakworks/gorm-tools/commit/56069b368a65243a35b5623a247e67ec8108fd8a)
- #474 Add tests for buildErrorMap & buildSuccessMap (#596) [link](https://github.com/yakworks/gorm-tools/commit/bb4733cc17efbd7cde034a08845be7b09bab9cdd)
- #285 Add test for id criteria restriction (#597) [link](https://github.com/yakworks/gorm-tools/commit/bf79eb821b2002c0d6baed4cf9f3f881c2ab89dc)
- Webclient okhttp (#594) [link](https://github.com/yakworks/gorm-tools/commit/d99970a7b449d345e7ee61734933b31473535467)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/600abe52f1223f7c7e85385063e93dadd7d68dd9)
- fix saml [link](https://github.com/yakworks/gorm-tools/commit/50d8d440f0194e5cfb64ca187749416ea79508a3)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/0d5a058a55277869a545a0ddb4ad13e2b3c31638)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/3ae7779c7a4c4f3903a9b6fa11a8fc553d4f816b)
- bump version so we dont stomp on moster dev [link](https://github.com/yakworks/gorm-tools/commit/e9fe146e0baca5a381cb7e9a2d951c9c722636b5)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/e5c0e7f126fec6ddded95c510ff47f2b7812ab10)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/13f693bb9079820c572bb8b716405cdba7c21eb2)
- get example working with JWT token, fix gradle to lock versions using resolutionStrategy. (#585) [link](https://github.com/yakworks/gorm-tools/commit/93bf62dc32649dea912c845c04fe46e7a033cd7c)
- remove old gradle ref to hello-boot-security-okta [link](https://github.com/yakworks/gorm-tools/commit/c58729ac499fd3553012f5711b848fb369435573)
- spring security 5.8 stock(#583) [link](https://github.com/yakworks/gorm-tools/commit/18a7ab71a1758cf85e1539ddce2c45a864cf1f97)
- Security Refactor (#579) [link](https://github.com/yakworks/gorm-tools/commit/3e55ad7bbfe5b597d7118b4c6795d6e606bdeb96)
- Security refactor common CurrentUser bean (#578) [link](https://github.com/yakworks/gorm-tools/commit/52e3903e244b87ad61355706ed0185273fdbf147)
- Merge branch 'master' into dev [link](https://github.com/yakworks/gorm-tools/commit/ced3ea6b44dceb83ce75c026f710331bd58daa3c)
- bump spring-kit to 5.0.8 and commons to 3.9 [link](https://github.com/yakworks/gorm-tools/commit/93693febba1c17ead55532297cfc5e3d2a5aec45)

### v7.3.27

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.26...v7.3.27)
- add fast exists query. (#602) [link](https://github.com/yakworks/gorm-tools/commit/6195c4f026eba82841c7692efcbb1667be846d70)
- Add query utils (#593) [link](https://github.com/yakworks/gorm-tools/commit/ecde38956fa74cf8858cfdc8ece04f6b1c48f60b)
- bump groovy-commons to 3.11 [link](https://github.com/yakworks/gorm-tools/commit/c4e38cfb01e18011a88113bcb6a1b899eea3b9c4)
- add bulkSaveEntity events that fire during bulk create or updates only. cleans up bulk method names for clarity. (#590) [link](https://github.com/yakworks/gorm-tools/commit/e5b87fc0c19264babce6ecd9ec18660d894dda8f)
- fix version back to 7.3.27 [link](https://github.com/yakworks/gorm-tools/commit/60418681938387dd1f198fa3212621b5ccacca03)
- bump version so we dont stomp on master dev. [link](https://github.com/yakworks/gorm-tools/commit/ecf11bc2ad30eb917f6126282673c67a4aa20361)
- bump commons to 3.10 (#589) [link](https://github.com/yakworks/gorm-tools/commit/94af7e49f6b9537fc45a0a603ef1fe657fe4fc72)
- createOrUpdate cleanup. doBulk tests. (#584) [link](https://github.com/yakworks/gorm-tools/commit/266e9fbfb6b740ae9fea7369ac3564e625ecfabd)

### v7.3.26

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.25...v7.3.26)
- bump commons to 3.9 and grails-kit to 5.0.8 [link](https://github.com/yakworks/gorm-tools/commit/9b482eee91759485304ec233814f0429f993ded5)
- release [link](https://github.com/yakworks/gorm-tools/commit/4701f61e7b15079c1ee2627f36b94659d67065a0)
- BuildSupport, hibernate-groovy-proxy,  RepoEntity now extends GormEntity (#575) [link](https://github.com/yakworks/gorm-tools/commit/7c36c947ab758b5c35d4080d94df12ab74acbb43)

### v7.3.25

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.23...v7.3.25)
- release [link](https://github.com/yakworks/gorm-tools/commit/a9182634113d3af0ee88aa936836839b37e34d73)
- move to yakworks packages, excel etl plugin and performance, use config objects(#569) [link](https://github.com/yakworks/gorm-tools/commit/9ab1a5877379502bb656320aef590b38235914d5)

### v7.3.23

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.3.22...v7.3.23)
- bump commons for generic collection fix, flush out UserTrait and AppU… (#558) [link](https://github.com/yakworks/gorm-tools/commit/f41f8e8e311ad31ba7c1a36380c6f2018cd774f6)

### v7.3.22

[Full Changelog](https://github.com/yakworks/gorm-tools/compare/v7.0.8-v.76...v7.3.22)
- release [link](https://github.com/yakworks/gorm-tools/commit/26e7a06d88155a02518f6ae60d93b564fe6660f9)
- Dev (#556) [link](https://github.com/yakworks/gorm-tools/commit/60e6a23fddf9ca540019664b47bc96030dd4dcc4)
- Merge branch 'release/7.0.8-v.x' [link](https://github.com/yakworks/gorm-tools/commit/b1df615560e1ead475cd7c8812573d68aecaf1e8)
- Fix unexpected logging (#547) [link](https://github.com/yakworks/gorm-tools/commit/bfa277ebec67e749d153a166035659235b1ea62d)
- dont log dup keys (#546) [link](https://github.com/yakworks/gorm-tools/commit/9ab4a05023ccb2adcb1dc1eb03a24af65d4c8d56)
- trigger for release 7.3.20 [link](https://github.com/yakworks/gorm-tools/commit/3e00c4ec430f3ba052dafdcf81fa653f412346b1)
- Grails 5.2, Gorm 7.3, Groovy 3 (#543) [link](https://github.com/yakworks/gorm-tools/commit/29954cda0741af3ecdfe0097f8be190fd9b2f52f)
