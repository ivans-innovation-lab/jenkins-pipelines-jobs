
// define the repos we want to build on CI
def repos = [ 'my-company-common','my-company-project-materialized-view','my-company-project-domain','my-company-blog-materialized-view','my-company-blog-domain','my-company-monolith' ]

// create a multibranch pipeline job for each of the repos
for (repo in repos)
{
  multibranchPipelineJob("${repo}") {
    
    scm{
      configure { gitScm ->
          gitScm / 'extensions' << 'hudson.plugins.git.extensions.impl.PathRestriction' {
            includedRegions('foo')
            excludedRegions('bar')
          }    
      }
    }

    // build master as well as feature branches 
    branchSources {
      git {
        remote("https://github.com/ivans-innovation-lab/${repo}.git")
        credentialsId('git')
        includes("master feature/*")
        configure { gitScm ->
          gitScm / 'extensions' << 'hudson.plugins.git.extensions.impl.PathRestriction' {
            includedRegions('foo')
            excludedRegions('bar')
          }        
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

