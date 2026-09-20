import { Camera, Mail, Shield, User2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import type { ProfileResponse } from "@/types/profile";

interface ProfileCardProps {
  profile: ProfileResponse;
  onUploadPhoto?: () => void;
}

export function ProfileCard({ profile, onUploadPhoto }: ProfileCardProps) {
  const initials = `${profile.firstName?.[0] ?? ""}${profile.lastName?.[0] ?? ""}`.toUpperCase();

  return (
    <Card>
      <CardContent className="flex flex-col items-center gap-4 pt-6 text-center">
        <button
          type="button"
          onClick={onUploadPhoto}
          className="group relative flex h-24 w-24 items-center justify-center overflow-hidden rounded-full bg-primary/10 text-2xl font-semibold text-primary"
          aria-label="Change profile photo"
        >
          {profile.profilePicture ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={profile.profilePicture} alt="Profile" className="h-full w-full object-cover" />
          ) : (
            <span>{initials || <User2 className="h-8 w-8" />}</span>
          )}
          {onUploadPhoto ? (
            <span className="absolute inset-0 hidden items-center justify-center bg-black/40 group-hover:flex">
              <Camera className="h-6 w-6 text-white" />
            </span>
          ) : null}
        </button>

        <div>
          <h2 className="text-lg font-semibold">
            {profile.firstName} {profile.lastName}
          </h2>
          <p className="flex items-center justify-center gap-1 text-sm text-muted-foreground">
            <Mail className="h-3.5 w-3.5" /> {profile.email}
          </p>
        </div>

        <div className="flex flex-wrap items-center justify-center gap-2">
          <Badge variant={profile.emailVerified ? "success" : "outline"}>
            {profile.emailVerified ? "Email Verified" : "Email Unverified"}
          </Badge>
          <Badge variant="outline">
            <Shield className="mr-1 h-3 w-3" /> KYC {profile.kycStatus}
          </Badge>
          <Badge variant={profile.profileCompleted ? "success" : "outline"}>
            {profile.profileCompleted ? "Profile Complete" : "Profile Incomplete"}
          </Badge>
        </div>
      </CardContent>
    </Card>
  );
}
