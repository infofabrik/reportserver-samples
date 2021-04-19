package net.datenwerke.rs.samples.admin.ldap

import java.util.Map.Entry
import java.util.logging.Level
import java.util.logging.Logger

import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.Attribute
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.LdapName

import net.datenwerke.security.service.usermanager.UserManagerService
import net.datenwerke.security.service.usermanager.entities.AbstractUserManagerNode
import net.datenwerke.security.service.usermanager.entities.Group
import net.datenwerke.security.service.usermanager.entities.OrganisationalUnit
import net.datenwerke.security.service.usermanager.entities.User




UserManagerService userManagerService = GLOBALS.getRsService(UserManagerService.class);

LdapUserLoader lul = new LdapUserLoader();

lul.setProviderUrl("ldap://directory.example.com:389");
lul.setSecurityPrincipal("CN=ldaptest,CN=Users,DC=directory,DC=example,DC=com");
lul.setSecurityCredentials("ldaptest");

lul.setLdapBase("OU=EXAMPLE,DC=directory,DC=example,DC=com");
OrganisationalUnit targetNode = (GLOBALS.findObject("/usermanager/external"));

if(null == targetNode){
    AbstractUserManagerNode umRoot = userManagerService.getRoots().get(0);
    targetNode = new OrganisationalUnit("external");
    umRoot.addChild(targetNode);
    userManagerService.persist(targetNode);
}

lul.setTargetNode(targetNode);
lul.run();


public class LdapUserLoader {

    private final Logger logger = Logger.getLogger(getClass().getName());


    private String ldapBase;
    private String ldapFilter = "(|(objectClass=organizationalUnit)(objectClass=user)(objectClass=group))";

    private String providerUrl;
    private String securityCredentials;

    private String securityPrincipal;
    private OrganisationalUnit targetNode;

    private boolean includeNamespace = false;

    private Map<String, AbstractUserManagerNode> guidMap;
    private Map<LdapName, AbstractUserManagerNode> nodesInDirectoryByName;
    private Map<String, AbstractUserManagerNode> nodesInDirectoryByGuid;
    private TreeMap<LdapName, SearchResult> searchResults;

    private List<AbstractUserManagerNode> removedNodes;
    private List<AbstractUserManagerNode> addedNodes;



    private class AdGUID {
        byte[] bytes;

        public AdGUID(byte[] bytes) {
            this.bytes = bytes;
        }

        private void addByte(StringBuffer sb, int k) {
            if(k<=0xF)
                sb.append("0");
            sb.append(Integer.toHexString(k));
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            addByte(sb, (int)bytes[3] & 0xFF);
            addByte(sb, (int)bytes[2] & 0xFF);
            addByte(sb, (int)bytes[1] & 0xFF);
            addByte(sb, (int)bytes[0] & 0xFF);
            sb.append("-");
            addByte(sb, (int)bytes[5] & 0xFF);
            addByte(sb, (int)bytes[4] & 0xFF);
            sb.append("-");
            addByte(sb, (int)bytes[7] & 0xFF);
            addByte(sb, (int)bytes[6] & 0xFF);
            sb.append("-");
            addByte(sb, (int)bytes[8] & 0xFF);
            addByte(sb, (int)bytes[9] & 0xFF);
            sb.append("-");
            addByte(sb, (int)bytes[10] & 0xFF);
            addByte(sb, (int)bytes[11] & 0xFF);
            addByte(sb, (int)bytes[12] & 0xFF);
            addByte(sb, (int)bytes[13] & 0xFF);
            addByte(sb, (int)bytes[14] & 0xFF);
            addByte(sb, (int)bytes[15] & 0xFF);

            return sb.toString();
        }

    }

    public LdapUserLoader() {

    }


    public String getLdapBase() {
        return ldapBase;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public String getSecurityCredentials() {
        return securityCredentials;
    }

    public String getSecurityPrincipal() {
        return securityPrincipal;
    }

    public void setLdapBase(String ldapBase) {
        this.ldapBase = ldapBase;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public void setSecurityCredentials(String securityCredentials) {
        this.securityCredentials = securityCredentials;
    }

    public void setSecurityPrincipal(String securityPrincipal) {
        this.securityPrincipal = securityPrincipal;
    }

    public void setTargetNode(OrganisationalUnit targetNode) {
        this.targetNode = targetNode;
    }

    public boolean isIncludeNamespace() {
        return includeNamespace;
    }

    public void setIncludeNamespace(boolean includeNamespace) {
        this.includeNamespace = includeNamespace;
    }

    public String getLdapFilter() {
        return ldapFilter;
    }

    public void setLdapFilter(String ldapFilter) {
        this.ldapFilter = ldapFilter;
    }



    private Properties compileProperties(){
        Properties props = new Properties();

        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.setProperty(Context.PROVIDER_URL, providerUrl);
        props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.jndi.url");
        props.setProperty(Context.REFERRAL, "throw");
        props.setProperty(Context.SECURITY_AUTHENTICATION, "simple");

        props.setProperty(Context.SECURITY_PRINCIPAL, securityPrincipal);
        props.setProperty(Context.SECURITY_CREDENTIALS, securityCredentials);

        /* return these as binary */
        props.put("java.naming.ldap.attributes.binary","objectGUID");
        return props;
    }

    private void createGuidMap(AbstractUserManagerNode current) {
        Map<String, AbstractUserManagerNode> map = new HashMap<>();
        createGuidMap(current, map );

        guidMap = map;
    }

    private void createGuidMap(AbstractUserManagerNode current, Map<String, AbstractUserManagerNode> map){
        map.put(current.getGuid(), current);

        for(AbstractUserManagerNode cn : current.getChildren()){
            createGuidMap(cn, map);
        }
    }

    private String getStringAttribute(SearchResult sr, String attributeName) throws NamingException{
        try{
            return sr.getAttributes().get(attributeName).get().toString();
        }catch(Exception e){
            logger.log(Level.WARNING,"failed to retrieve attribute '" + attributeName + "' from " + sr.getNameInNamespace(), e);
            return null;
        }
    }

    private String getGuid(SearchResult sr) throws NamingException{
        try{
            AdGUID guid = new AdGUID((byte[]) sr.getAttributes().get("objectGUID").get());
            return guid.toString();
        }catch(Exception e){
            throw new RuntimeException("failed to retrieve objectGUID from " + sr.getNameInNamespace(), e);
        }
    }

    private void loadFromDirectory() throws NamingException {
        Properties props = compileProperties();
        String originBase = this.providerUrl.endsWith("/")?providerUrl:providerUrl + "/";

        this.nodesInDirectoryByName = new HashMap<>();
        this.nodesInDirectoryByGuid = new HashMap<>();
        this.addedNodes = new ArrayList<>();
        this.removedNodes = new ArrayList<>();

        DirContext ctx = null ;

        try {
            ctx = new InitialDirContext(props);

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            LdapName ldapBaseName = new LdapName(getLdapBase());
            NamingEnumeration<SearchResult> results = ctx.search(ldapBaseName, this.ldapFilter, searchControls);

            /* order search results by name to make sure children never get processed before their parent */
            searchResults = new TreeMap<>();
            while (results.hasMoreElements()) {
                SearchResult sr = (SearchResult) results.next();
                searchResults.put(new LdapName(sr.getNameInNamespace()), sr);
            }

            for(SearchResult sr : searchResults.values()){
                try {
                    LdapName nodeName = new LdapName(isIncludeNamespace() ? sr.getNameInNamespace() : sr.getName());
                    LdapName nodeNameInNamespace = new LdapName(sr.getNameInNamespace());

                    /* skip empty nodes */
                    if(nodeName.size() == 0)
                        continue;


                    /* get parent node */
                    LdapName parentName = (LdapName) nodeNameInNamespace.getPrefix(Math.max(0, nodeNameInNamespace.size() - 1));
                    AbstractUserManagerNode parent = this.nodesInDirectoryByName.get(parentName);
                    if(null == parent){
                        if(parentName.equals(new LdapName(ldapBase))){
                            /* root node */
                            parent = targetNode;
                        }else{
                            throw new IllegalStateException("Missing parent for " + sr.getNameInNamespace());
                        }
                    }

                    /* create node */
                    Attribute objectClass = sr.getAttributes().get("objectClass");
                    AbstractUserManagerNode umNode = null;
                    if(objectClass.contains("organizationalUnit")) {
                        umNode = createOUNode(sr, parent);

                    } else if(objectClass.contains("user")) {
                        umNode = createUserNode(sr, parent);

                    } else if(objectClass.contains("group")){
                        umNode = createGroupNode(sr, parent);
                    }

                    /* set common attributes */
                    umNode.setWriteProtection(true);
                    umNode.setGuid(getGuid(sr));
                    umNode.setOrigin(originBase + sr.getNameInNamespace());

                    nodesInDirectoryByName.put(new LdapName(sr.getNameInNamespace()), umNode);
                    nodesInDirectoryByGuid.put(getGuid(sr), umNode);
                }catch(Exception e){
                    throw new RuntimeException("Error processing search result: " + sr.getNameInNamespace() , e);
                }
            }


        }finally{
            try {
                if(null != ctx)
                    ctx.close();
            } catch (NamingException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }


    private AbstractUserManagerNode createGroupNode(SearchResult sr, AbstractUserManagerNode parent) throws NamingException {
        Group node = (Group) guidMap.get(getGuid(sr));
        if(null == node){
            node = new Group();
            addedNodes.add(node);
        }
        parent.addChild(node);

        /* copy Group attributes */
        node.setName(getStringAttribute(sr, "name"));

        return node;
    }


    private AbstractUserManagerNode createUserNode(SearchResult sr, AbstractUserManagerNode parent) throws NamingException {
        User node = (User) guidMap.get(getGuid(sr));
        if(null == node){
            node = new User();
            addedNodes.add(node);
        }
        parent.addChild(node);

        /* copy User attributes */
        node.setFirstname(getStringAttribute(sr, "givenName"));
        node.setLastname(getStringAttribute(sr, "sn"));
        node.setUsername(getStringAttribute(sr, "sAMAccountName"));

        return node;
    }

    private AbstractUserManagerNode createOUNode(SearchResult sr, AbstractUserManagerNode parent) throws NamingException {
        OrganisationalUnit node = (OrganisationalUnit) guidMap.get(getGuid(sr));
        if(null == node){
            node = new OrganisationalUnit();
            addedNodes.add(node);
        }
        parent.addChild(node);

        /* copy OU attributes */
        node.setName(getStringAttribute(sr, "name"));

        return node;
    }


    private void postprocessGroups() throws NamingException {

        /* clear */
        for(Entry<LdapName, AbstractUserManagerNode> entry : nodesInDirectoryByName.entrySet()){
            if(entry.getValue() instanceof Group){
                Group group = (Group) entry.getValue();
                group.getUsers().clear();
                group.getOus().clear();
                group.getReferencedGroups().clear();
            }
        }

        /* add appropriate users */
        for(Entry<LdapName, AbstractUserManagerNode> entry : nodesInDirectoryByName.entrySet()){
            if(entry.getValue() instanceof Group){
                Group group = (Group) entry.getValue();
                SearchResult sr = searchResults.get(entry.getKey());
                if(null != sr.getAttributes().get("member")){
                    NamingEnumeration<?> members = sr.getAttributes().get("member").getAll();
                    while(members.hasMore()){
                        LdapName memberName = new LdapName(members.next().toString());
                        AbstractUserManagerNode member = nodesInDirectoryByName.get(memberName);
                        if(null != member){
                            if(member instanceof User)
                                group.addUser((User) member);
                            if(member instanceof OrganisationalUnit)
                                group.addOu((OrganisationalUnit) member);
                            if(member instanceof Group)
                                group.addReferencedGroup((Group) member);
                        }
                    }
                }
            }
        }
    }

    private void printTree(AbstractUserManagerNode current){
        StringBuilder sb = new StringBuilder();
        List<AbstractUserManagerNode> rl = current.getRootLine();
        Collections.reverse(rl);
        for(AbstractUserManagerNode node : rl){
            sb.append(node.getName()).append(".");
        }
        sb.append(current.getName() + " [" + current.getClass().getSimpleName() + "]" );

        if(current instanceof Group){
            Group group = (Group) current;
            sb.append(" (").append(group.getUsers().size() + group.getOus().size() + group.getReferencedGroups().size()).append(" members)");
        }

        System.out.println(sb.toString());

        for(AbstractUserManagerNode cn : current.getChildren()){
            printTree(cn);
        }
    }


    private void deleteRemovedUsers(AbstractUserManagerNode current) {
        for(AbstractUserManagerNode c : current.getChildren()){
            deleteRemovedUsers(c);
        }

        if(null != current.getOrigin() && current.getOrigin().startsWith(providerUrl) && !nodesInDirectoryByGuid.containsKey(current.getGuid())){
            current.getParent().removeChild(current);
            removedNodes.add(current);
        }
    }

    public void run() throws NamingException{
        createGuidMap(targetNode);

        loadFromDirectory();
        postprocessGroups();

        deleteRemovedUsers(targetNode);

        System.out.println("Retrieved nodes from directory: " + nodesInDirectoryByGuid.size() );
        System.out.println("Nodes added: " + addedNodes.size() );
        System.out.println("Nodes removed: " + removedNodes.size() );
        int overallCount = countNodes(targetNode) - 1;
        System.out.println("Overall nodes in rs: " + overallCount);

        if(overallCount != nodesInDirectoryByGuid.size())
            throw new RuntimeException("Failed to import user data from directory");
        else
            System.out.println("done.");

        //      printTree(targetNode);
    }


    private int countNodes(AbstractUserManagerNode current) {
        int i = 1;
        for(AbstractUserManagerNode n : current.getChildren()){
            i += countNodes(n);
        }
        return i;
    }

}