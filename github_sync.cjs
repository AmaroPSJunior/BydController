const { Octokit } = require("@octokit/rest");
const fs = require("fs");
const path = require("path");
const { glob } = require("glob");

async function sync() {
  const token = process.env.GITHUB_TOKEN;
  const repoFull = process.env.GITHUB_REPO; // ex: user/repo

  if (!token || !repoFull) {
    console.error("GITHUB_TOKEN or GITHUB_REPO not set");
    process.exit(1);
  }

  const [owner, repo] = repoFull.split("/");
  const octokit = new Octokit({ auth: token });

  console.log(`Syncing to ${owner}/${repo}...`);

  // Get all files
  const files = await glob("**/*", {
    ignore: [
      "node_modules/**",
      "dist/**",
      ".git/**",
      "package-lock.json",
      "build/**",
      ".next/**",
    ],
    nodir: true,
  });

  const branch = "main";
  
  // Get latest commit SHA for the branch
  let baseTree;
  try {
    const { data: refData } = await octokit.git.getRef({
      owner,
      repo,
      ref: `heads/${branch}`,
    });
    const { data: commitData } = await octokit.git.getCommit({
      owner,
      repo,
      commit_sha: refData.object.sha,
    });
    baseTree = commitData.tree.sha;
  } catch (e) {
    console.error("Error getting base tree. Ensure the repo and branch exist.");
    process.exit(1);
  }

  const blobs = await Promise.all(
    files.map(async (filePath) => {
      const content = fs.readFileSync(filePath, "utf8");
      const { data } = await octokit.git.createBlob({
        owner,
        repo,
        content,
        encoding: "utf-8",
      });
      return {
        path: filePath,
        mode: "100644",
        type: "blob",
        sha: data.sha,
      };
    })
  );

  const { data: newTree } = await octokit.git.createTree({
    owner,
    repo,
    base_tree: baseTree,
    tree: blobs,
  });

  const { data: newCommit } = await octokit.git.createCommit({
    owner,
    repo,
    message: `Auto-sync: ${new Date().toISOString()}`,
    tree: newTree.sha,
    parents: [baseTree],
  });

  await octokit.git.updateRef({
    owner,
    repo,
    ref: `heads/${branch}`,
    sha: newCommit.sha,
  });

  console.log("Success! Files synced via API.");

  // Post a comment to the repository (e.g., on a PR or just a general comment if supported, 
  // but usually comments are on issues/PRs. Let's try to comment on the commit itself)
  await octokit.repos.createCommitComment({
    owner,
    repo,
    commit_sha: newCommit.sha,
    body: "🚀 Sincronização automática concluída via AI Coding Agent!",
  });
}

sync().catch((err) => {
  console.error(err);
  process.exit(1);
});
