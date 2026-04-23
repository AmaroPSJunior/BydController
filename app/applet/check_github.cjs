const { Octokit } = require("@octokit/rest");

async function check() {
  const token = process.env.GITHUB_TOKEN;
  const repoFull = process.env.GITHUB_REPO;
  const [owner, repo] = repoFull.split("/");
  const octokit = new Octokit({ auth: token });

  console.log(`Checking branches for ${owner}/${repo}...`);
  try {
    const { data: branches } = await octokit.repos.listBranches({ owner, repo });
    console.log("Branches:", branches.map(b => b.name));
    
    const { data: repoData } = await octokit.repos.get({ owner, repo });
    console.log("Default branch:", repoData.default_branch);
  } catch (e) {
    console.error("Error:", e.message);
  }
}

check();
