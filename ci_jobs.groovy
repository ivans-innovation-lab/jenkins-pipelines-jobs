import org.xml.sax.SAXException;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.IOException;

// define the repos we want to build on CI
def repos = [ 'my-company-common','my-company-project-materialized-view','my-company-project-domain','my-company-blog-materialized-view','my-company-blog-domain','my-company-monolith' ]

// create a multibranch pipeline job for each of the repos
for (repo in repos)
{
  multibranchPipelineJob("${repo}") {
    // build master as well as feature branches 
    branchSources {
      git {
        remote("https://github.com/ivans-innovation-lab/${repo}.git")
        credentialsId('git')
        includes("master feature/*")
        configure { project -> 
            def branches = (project / 'sources' / 'data')
            for(int i = 0 ; i < branches.getLength();i++) {
              Element el = (Element) branches.item(i);
              System.out.println(el.getTextContent());
            }
            def branch = branches.getAt("0")
           System.out.println(branch.getTextContent());
            
              
         }
      }
    }
    // check every minute for scm changes as well as new / deleted branches
    triggers {
      periodic(1)
    }
    // don't keep build jobs for deleted branches
    orphanedItemStrategy {
      discardOldItems {
        numToKeep(0)
      }
    }
  }
  // automatically queue the job after the initial creation
  if (!jenkins.model.Jenkins.instance.getItemByFullName("${repo}")) {
    queue("${repo}")
  }
  
}
