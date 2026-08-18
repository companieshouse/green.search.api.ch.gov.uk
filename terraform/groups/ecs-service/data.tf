
data "vault_generic_secret" "stack_secrets" {
  path = "applications/${var.aws_profile}/${var.environment}/${local.stack_name}-stack"
}

data "vault_generic_secret" "service_secrets" {
  path = "applications/${var.aws_profile}/${var.environment}/${local.stack_name}-stack/${local.service_name}"
}

data "aws_kms_key" "kms_key" {
  key_id = local.kms_alias
}

data "aws_vpc" "vpc" {
  filter {
    name   = "tag:Name"
    values = [local.vpc_name]
  }
}

#Get application subnet IDs
data "aws_subnets" "application" {
  filter {
    name   = "tag:Name"
    values = [local.application_subnet_pattern]
  }
}

data "aws_ecs_cluster" "ecs_cluster" {
  cluster_name = "${local.name_prefix}-cluster"
}

data "aws_iam_role" "ecs_cluster_iam_role" {
  name = "${local.name_prefix}-ecs-task-execution-role"
}

data "aws_lb" "service_lb" {
  name = "${var.environment}-chs-apichgovuk"
}

data "aws_lb_listener" "service_lb_listener" {
  load_balancer_arn = data.aws_lb.service_lb.arn
  port              = 443
}

data "aws_lb" "secondary_lb" {
  name = "${var.environment}-chs-apichgovuk-private"
}

data "aws_lb_listener" "secondary_lb_listener" {
  load_balancer_arn = data.aws_lb.secondary_lb.arn
  port              = 443
}

# retrieve all global secrets for this env using global path
data "aws_ssm_parameters_by_path" "global_secrets" {
  path = "/${local.global_prefix}"
}
# create a list of secrets names to retrieve them in a nicer format and lookup each secret by name
data "aws_ssm_parameter" "global_secret" {
  for_each = toset(data.aws_ssm_parameters_by_path.global_secrets.names)
  name     = each.key
}

data "aws_opensearch_domain" "alphabetical_search" {
  domain_name = local.open_search_domain_name
}

data "aws_iam_policy_document" "task_policy" {
  statement {
    sid    = "AllowESHttpRead"
    effect = "Allow"
    actions = [
      "es:ESHttpGet",
      "es:ESHttpHead"
    ]
    resources = [
      data.aws_opensearch_domain.alphabetical_search.arn
    ]
  }
  statement {
    sid    = "AllowESHttpWrite"
    effect = "Allow"
    actions = [
      "es:ESHttpPost",
      "es:ESHttpPut",
      "es:ESHttpDelete",
      "es:ESHttpPatch"
    ]
    resources = [
      data.aws_opensearch_domain.alphabetical_search.arn
    ]
  }
}

data "aws_iam_policy_document" "task_assume" {
  statement {
    sid     = "AllowTaskAssumeRole"
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}
