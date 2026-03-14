#!/bin/sh
# localstack-init.sh — runs inside LocalStack on startup.
# Creates only S3 resources (SNS/SQS handled by Kafka locally).
# This script is idempotent: safe to run multiple times.

echo "=== FinalBid LocalStack Init ==="

echo "[1/1] Creating S3 bucket: finalbid-images"
aws --endpoint-url=http://localhost:4566 s3 mb s3://finalbid-images 2>/dev/null || echo "  → Bucket already exists, skipping."

echo "=== LocalStack Init Complete ==="
