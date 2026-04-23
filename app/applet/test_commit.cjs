const { Octokit } = require("@octokit/rest");

async function test() {
  const token = process.env.GITHUB_TOKEN;
  const repoFull = process.env.GITHUB_REPO;
  const [owner, repo] = repoFull.split("/");
  const octokit = new Octokit({ auth: token });

  const { data: refData } = await octokit.git.getRef({
    owner,
    repo,
    ref: `heads/main`,
  });
  const parentSha = refData.object.sha;
  
  const { data: commitData } = await octokit.git.getCommit({
    owner,
    repo,
    commit_sha: parentSha,
  });
  const treeSha = commitData.tree.sha;

  console.log(`Parent: ${parentSha}, Tree: ${treeSha}`);

  // Create a minimal tree
  const { data: newTree } = await octokit.git.createTree({
    owner,
    repo,
    base_tree: treeSha,
    tree: [{
      path: "sync_test.txt",
      mode: "100644",
      type: "blob",
      content: `Sync test at ${new Date().toISOString()}`
    }]
  });

  console.log(`New Tree: ${newTree.sha}`);

  // Create commit
  const { data: newCommit } = await octokit.git.createCommit({
    owner,
    repo,
    message: "Test sync commit",
    tree: newTree.sha,
    parents: [parentSha]
  });

  console.log(`New Commit: ${newCommit.sha}`);

  await octokit.git.updateRef({
    owner,
    repo,
    ref: `heads/main`,
    sha: newCommit.sha
  });

  console.log("TEST SUCCESSFUL");
}

test().catch(e => console.error(e));
